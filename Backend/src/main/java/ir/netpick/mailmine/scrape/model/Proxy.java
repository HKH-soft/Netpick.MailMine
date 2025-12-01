package ir.netpick.mailmine.scrape.model;

import ir.netpick.mailmine.common.BaseEntity;
import ir.netpick.mailmine.common.enums.ProxyProtocol;
import ir.netpick.mailmine.common.enums.ProxyStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "proxy", indexes = {
        @Index(name = "idx_proxy_status", columnList = "status"),
        @Index(name = "idx_proxy_protocol", columnList = "protocol")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uniq_proxy_host_port", columnNames = { "host", "port" })
})
public class Proxy extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "protocol", nullable = false)
    private ProxyProtocol protocol = ProxyProtocol.SOCKS5;

    @Column(name = "host", nullable = false)
    private String host;

    @Column(name = "port", nullable = false)
    private Integer port;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProxyStatus status = ProxyStatus.UNTESTED;

    @Column(name = "last_tested_at")
    private LocalDateTime lastTestedAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "success_count")
    private Integer successCount = 0;

    @Column(name = "failure_count")
    private Integer failureCount = 0;

    @Column(name = "avg_response_time_ms")
    private Long avgResponseTimeMs;

    // ==================== V2Ray specific fields ====================

    /**
     * UUID for VLESS/VMess protocols
     */
    @Column(name = "uuid")
    private String uuid;

    /**
     * Encryption method (e.g., "none", "auto", "aes-128-gcm", "chacha20-poly1305")
     */
    @Column(name = "encryption")
    private String encryption;

    /**
     * Transport type (tcp, ws, grpc, http, quic)
     */
    @Column(name = "transport")
    private String transport;

    /**
     * TLS/security setting (none, tls, reality)
     */
    @Column(name = "security")
    private String security;

    /**
     * SNI for TLS
     */
    @Column(name = "sni")
    private String sni;

    /**
     * WebSocket path or gRPC service name
     */
    @Column(name = "path")
    private String path;

    /**
     * Host header for WebSocket/HTTP
     */
    @Column(name = "ws_host")
    private String wsHost;

    /**
     * ALPN setting
     */
    @Column(name = "alpn")
    private String alpn;

    /**
     * Fingerprint for TLS
     */
    @Column(name = "fingerprint")
    private String fingerprint;

    /**
     * Public key for Reality
     */
    @Column(name = "public_key")
    private String publicKey;

    /**
     * Short ID for Reality
     */
    @Column(name = "short_id")
    private String shortId;

    /**
     * AlterID for VMess (legacy)
     */
    @Column(name = "alter_id")
    private Integer alterId;

    /**
     * Flow control for VLESS (e.g., "xtls-rprx-vision")
     */
    @Column(name = "flow")
    private String flow;

    /**
     * Original share link (vless://, vmess://, ss://)
     */
    @Column(name = "original_link", length = 2048)
    private String originalLink;

    /**
     * Local port when running as V2Ray client
     */
    @Column(name = "local_port")
    private Integer localPort;

    public Proxy() {
    }

    public Proxy(ProxyProtocol protocol, String host, Integer port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public Proxy(ProxyProtocol protocol, String host, Integer port, String username, String password) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the proxy URL for Playwright.
     * For V2Ray protocols, returns the local SOCKS5 proxy URL.
     * For standard proxies, returns the direct URL.
     */
    public String toProxyUrl() {
        // V2Ray protocols use local proxy
        if (isV2RayProtocol()) {
            if (localPort == null) {
                throw new IllegalStateException(
                        "V2Ray proxy requires localPort to be set. Call V2RayClientService.startProxy() first.");
            }
            return "socks5://127.0.0.1:" + localPort;
        }

        // Standard proxy URL
        StringBuilder sb = new StringBuilder();
        sb.append(protocol.name().toLowerCase()).append("://");
        if (username != null && !username.isBlank()) {
            sb.append(username);
            if (password != null && !password.isBlank()) {
                sb.append(":").append(password);
            }
            sb.append("@");
        }
        sb.append(host).append(":").append(port);
        return sb.toString();
    }

    /**
     * Returns a display-friendly string for logging.
     * Unlike toProxyUrl(), this never throws and shows the remote server info.
     */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol.name()).append(" ");
        sb.append(host).append(":").append(port);
        if (description != null && !description.isBlank()) {
            sb.append(" (").append(description).append(")");
        }
        return sb.toString();
    }

    /**
     * Check if this proxy uses a V2Ray protocol
     */
    public boolean isV2RayProtocol() {
        return protocol == ProxyProtocol.VLESS ||
                protocol == ProxyProtocol.VMESS ||
                protocol == ProxyProtocol.SHADOWSOCKS ||
                protocol == ProxyProtocol.TROJAN;
    }

    /**
     * Parse a proxy URL string into a Proxy object
     * Supports formats:
     * - socks5://host:port
     * - socks5://user:pass@host:port
     * - http://host:port
     * - host:port (defaults to SOCKS5)
     * - vless://uuid@host:port?params#name
     * - vmess://base64encoded
     * - ss://base64encoded#name
     * - trojan://password@host:port?params#name
     * Also handles messy formats like: ss://...@host:port # [ Comment ] 🔒
     */
    public static Proxy fromUrl(String proxyUrl) {
        // Clean up the URL - remove extra spaces, comments, emojis outside the standard
        // format
        String url = cleanProxyUrl(proxyUrl);

        // V2Ray protocols
        if (url.startsWith("vless://")) {
            return parseVlessUrl(url);
        } else if (url.startsWith("vmess://")) {
            return parseVmessUrl(url);
        } else if (url.startsWith("ss://")) {
            return parseShadowsocksUrl(url);
        } else if (url.startsWith("trojan://")) {
            return parseTrojanUrl(url);
        }

        // Standard proxy parsing
        return parseStandardProxy(url);
    }

    /**
     * Clean up a proxy URL by removing extra formatting, comments, emojis outside
     * standard format
     * Handles cases like: ss://base64@host:port # [ Comment ] 🔒
     */
    private static String cleanProxyUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalArgumentException("Proxy URL cannot be empty");
        }

        String url = rawUrl.trim();

        // For V2Ray protocols, handle space-separated comments
        // Pattern: protocol://content SPACE extra_stuff
        // We want to keep only protocol://content and convert space+text to proper
        // #fragment

        if (url.startsWith("ss://") || url.startsWith("vless://") ||
                url.startsWith("vmess://") || url.startsWith("trojan://")) {

            // Find the actual protocol content before any space-separated comment
            // But preserve proper #fragment which is part of the URL standard

            // First, check if there's a space before any # (malformed comment)
            int spaceIdx = findFirstSpaceNotInFragment(url);
            if (spaceIdx > 0) {
                String mainPart = url.substring(0, spaceIdx).trim();
                String commentPart = url.substring(spaceIdx).trim();

                // If main part doesn't have #, and comment looks like a name, add it
                if (!mainPart.contains("#") && !commentPart.isEmpty()) {
                    // Clean the comment - remove leading # or other chars, emojis are ok
                    commentPart = commentPart.replaceFirst("^[#\\s]+", "").trim();
                    if (!commentPart.isEmpty()) {
                        // URL-encode the comment for the fragment
                        try {
                            commentPart = java.net.URLEncoder.encode(commentPart, "UTF-8");
                        } catch (Exception e) {
                            // Keep as-is if encoding fails
                        }
                        url = mainPart + "#" + commentPart;
                    } else {
                        url = mainPart;
                    }
                } else {
                    url = mainPart;
                }
            }
        } else {
            // For standard proxies (http, socks), just take the first part before space
            int spaceIdx = url.indexOf(' ');
            if (spaceIdx > 0) {
                url = url.substring(0, spaceIdx);
            }
        }

        return url.trim();
    }

    /**
     * Find the first space that's not inside a proper URL fragment
     */
    private static int findFirstSpaceNotInFragment(String url) {
        int hashIdx = url.indexOf('#');
        int spaceIdx = url.indexOf(' ');

        if (spaceIdx < 0) {
            return -1; // No space
        }

        if (hashIdx < 0 || spaceIdx < hashIdx) {
            // Space comes before # (or no #), this is the malformed case
            return spaceIdx;
        }

        // Space is after #, which is inside the fragment - that's valid
        return -1;
    }

    private static Proxy parseStandardProxy(String url) {
        Proxy proxy = new Proxy();

        // Determine protocol
        if (url.startsWith("socks5://")) {
            proxy.setProtocol(ProxyProtocol.SOCKS5);
            url = url.substring(9);
        } else if (url.startsWith("socks4://")) {
            proxy.setProtocol(ProxyProtocol.SOCKS4);
            url = url.substring(9);
        } else if (url.startsWith("http://")) {
            proxy.setProtocol(ProxyProtocol.HTTP);
            url = url.substring(7);
        } else if (url.startsWith("https://")) {
            proxy.setProtocol(ProxyProtocol.HTTPS);
            url = url.substring(8);
        } else {
            proxy.setProtocol(ProxyProtocol.SOCKS5); // Default
        }

        // Check for auth (user:pass@)
        if (url.contains("@")) {
            String[] authAndHost = url.split("@", 2);
            String auth = authAndHost[0];
            url = authAndHost[1];

            if (auth.contains(":")) {
                String[] userPass = auth.split(":", 2);
                proxy.setUsername(userPass[0]);
                proxy.setPassword(userPass[1]);
            } else {
                proxy.setUsername(auth);
            }
        }

        // Parse host:port
        String[] hostPort = url.split(":");
        proxy.setHost(hostPort[0]);
        proxy.setPort(Integer.parseInt(hostPort[1]));

        return proxy;
    }

    /**
     * Parse VLESS URL:
     * vless://uuid@host:port?type=tcp&security=tls&sni=example.com#name
     */
    private static Proxy parseVlessUrl(String url) {
        Proxy proxy = new Proxy();
        proxy.setProtocol(ProxyProtocol.VLESS);
        proxy.setOriginalLink(url);

        try {
            // Remove vless:// prefix
            String content = url.substring(8);

            // Extract name (after #)
            String name = null;
            if (content.contains("#")) {
                String[] parts = content.split("#", 2);
                content = parts[0];
                name = java.net.URLDecoder.decode(parts[1], "UTF-8");
                proxy.setDescription(name);
            }

            // Extract query params
            String params = "";
            if (content.contains("?")) {
                String[] parts = content.split("\\?", 2);
                content = parts[0];
                params = parts[1];
            }

            // Parse uuid@host:port
            String[] uuidAndHost = content.split("@", 2);
            proxy.setUuid(uuidAndHost[0]);

            String hostPort = uuidAndHost[1];
            String[] hp = hostPort.split(":");
            proxy.setHost(hp[0]);
            proxy.setPort(Integer.parseInt(hp[1]));

            // Parse query params
            parseQueryParams(proxy, params);

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid VLESS URL: " + url, e);
        }

        return proxy;
    }

    /**
     * Parse VMess URL: vmess://base64json
     */
    private static Proxy parseVmessUrl(String url) {
        Proxy proxy = new Proxy();
        proxy.setProtocol(ProxyProtocol.VMESS);
        proxy.setOriginalLink(url);

        try {
            // Remove vmess:// prefix and decode base64
            String base64 = url.substring(8);
            String json = new String(java.util.Base64.getDecoder().decode(base64));

            // Parse JSON using simple string parsing (avoid adding dependency)
            proxy.setHost(extractJsonValue(json, "add"));
            proxy.setPort(Integer.parseInt(extractJsonValue(json, "port")));
            proxy.setUuid(extractJsonValue(json, "id"));
            proxy.setAlterId(Integer.parseInt(extractJsonValueOrDefault(json, "aid", "0")));
            proxy.setTransport(extractJsonValueOrDefault(json, "net", "tcp"));
            proxy.setSecurity(extractJsonValueOrDefault(json, "tls", ""));
            proxy.setPath(extractJsonValueOrDefault(json, "path", ""));
            proxy.setWsHost(extractJsonValueOrDefault(json, "host", ""));
            proxy.setSni(extractJsonValueOrDefault(json, "sni", ""));
            proxy.setDescription(extractJsonValueOrDefault(json, "ps", ""));

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid VMess URL: " + url, e);
        }

        return proxy;
    }

    /**
     * Parse Shadowsocks URL: ss://base64(method:password)@host:port#name
     * or ss://base64(method:password@host:port)#name
     */
    private static Proxy parseShadowsocksUrl(String url) {
        Proxy proxy = new Proxy();
        proxy.setProtocol(ProxyProtocol.SHADOWSOCKS);
        proxy.setOriginalLink(url);

        try {
            String content = url.substring(5); // Remove ss://

            // Extract name (after #)
            if (content.contains("#")) {
                String[] parts = content.split("#", 2);
                content = parts[0];
                try {
                    proxy.setDescription(java.net.URLDecoder.decode(parts[1], "UTF-8"));
                } catch (Exception e) {
                    proxy.setDescription(parts[1]); // Use as-is if decode fails
                }
            }

            // Try SIP002 format: base64@host:port
            if (content.contains("@")) {
                String[] parts = content.split("@", 2);
                String base64Part = parts[0];
                String hostPortPart = parts[1];

                // Decode the base64 part (method:password)
                String decoded = new String(java.util.Base64.getDecoder().decode(base64Part));
                String[] methodPass = decoded.split(":", 2);
                proxy.setEncryption(methodPass[0]);
                proxy.setPassword(methodPass[1]);

                // Parse host:port - extract only digits for port
                String[] hostPort = hostPortPart.split(":", 2);
                proxy.setHost(hostPort[0].trim());
                // Extract only the numeric part of the port (in case of trailing garbage)
                String portStr = hostPort[1].replaceAll("[^0-9].*$", "");
                proxy.setPort(Integer.parseInt(portStr));
            } else {
                // Legacy format: base64(method:password@host:port)
                String decoded = new String(java.util.Base64.getDecoder().decode(content));
                String[] methodAndRest = decoded.split(":", 2);
                proxy.setEncryption(methodAndRest[0]);

                String rest = methodAndRest[1];
                String[] passAndHost = rest.split("@", 2);
                proxy.setPassword(passAndHost[0]);

                String[] hostPort = passAndHost[1].split(":");
                proxy.setHost(hostPort[0]);
                proxy.setPort(Integer.parseInt(hostPort[1]));
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Shadowsocks URL: " + url, e);
        }

        return proxy;
    }

    /**
     * Parse Trojan URL: trojan://password@host:port?sni=xxx#name
     */
    private static Proxy parseTrojanUrl(String url) {
        Proxy proxy = new Proxy();
        proxy.setProtocol(ProxyProtocol.TROJAN);
        proxy.setOriginalLink(url);

        try {
            String content = url.substring(9); // Remove trojan://

            // Extract name
            if (content.contains("#")) {
                String[] parts = content.split("#", 2);
                content = parts[0];
                proxy.setDescription(java.net.URLDecoder.decode(parts[1], "UTF-8"));
            }

            // Extract params
            String params = "";
            if (content.contains("?")) {
                String[] parts = content.split("\\?", 2);
                content = parts[0];
                params = parts[1];
            }

            // Parse password@host:port
            String[] passAndHost = content.split("@", 2);
            proxy.setPassword(passAndHost[0]);

            String[] hostPort = passAndHost[1].split(":");
            proxy.setHost(hostPort[0]);
            proxy.setPort(Integer.parseInt(hostPort[1]));

            parseQueryParams(proxy, params);

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Trojan URL: " + url, e);
        }

        return proxy;
    }

    private static void parseQueryParams(Proxy proxy, String params) {
        if (params == null || params.isEmpty())
            return;

        for (String param : params.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length != 2)
                continue;

            String key = kv[0];
            String value = kv[1];

            switch (key) {
                case "type" -> proxy.setTransport(value);
                case "security" -> proxy.setSecurity(value);
                case "sni" -> proxy.setSni(value);
                case "path" -> proxy.setPath(value);
                case "host" -> proxy.setWsHost(value);
                case "alpn" -> proxy.setAlpn(value);
                case "fp" -> proxy.setFingerprint(value);
                case "pbk" -> proxy.setPublicKey(value);
                case "sid" -> proxy.setShortId(value);
                case "encryption" -> proxy.setEncryption(value);
                case "flow" -> proxy.setFlow(value);
            }
        }
    }

    private static String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"?([^\"\\},]+)\"?";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1).trim();
        }
        throw new IllegalArgumentException("Key not found: " + key);
    }

    private static String extractJsonValueOrDefault(String json, String key, String defaultValue) {
        try {
            return extractJsonValue(json, key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public void recordSuccess(long responseTimeMs) {
        this.successCount++;
        this.lastUsedAt = LocalDateTime.now();
        if (this.avgResponseTimeMs == null) {
            this.avgResponseTimeMs = responseTimeMs;
        } else {
            this.avgResponseTimeMs = (this.avgResponseTimeMs + responseTimeMs) / 2;
        }
    }

    public void recordFailure() {
        this.failureCount++;
        this.lastUsedAt = LocalDateTime.now();
    }
}
