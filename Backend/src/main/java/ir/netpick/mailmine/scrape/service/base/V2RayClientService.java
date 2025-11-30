package ir.netpick.mailmine.scrape.service.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ir.netpick.mailmine.common.enums.ProxyProtocol;
import ir.netpick.mailmine.scrape.model.Proxy;
import ir.netpick.mailmine.scrape.repository.ProxyRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages V2Ray/Xray client processes for VLESS, VMess, Shadowsocks, and Trojan
 * proxies.
 * Each proxy runs on its own local port.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class V2RayClientService {

    private final ProxyRepository proxyRepository;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Value("${v2ray.executable:xray}")
    private String v2rayExecutable;

    @Value("${v2ray.config-dir:./v2ray-configs}")
    private String configDir;

    @Value("${v2ray.base-port:20000}")
    private int basePort;

    // Running processes: proxyId -> Process
    private final Map<UUID, Process> runningProcesses = new ConcurrentHashMap<>();

    // Port allocator
    private final AtomicInteger portCounter = new AtomicInteger(0);
    private final Set<Integer> usedPorts = ConcurrentHashMap.newKeySet();

    /**
     * Start a V2Ray client for the given proxy
     */
    public int startProxy(Proxy proxy) {
        if (!proxy.isV2RayProtocol()) {
            throw new IllegalArgumentException("Proxy is not a V2Ray protocol");
        }

        // Check if already running
        if (runningProcesses.containsKey(proxy.getId())) {
            log.info("Proxy {} already running on port {}", proxy.getId(), proxy.getLocalPort());
            return proxy.getLocalPort();
        }

        // Allocate port
        int localPort = allocatePort();
        proxy.setLocalPort(localPort);

        try {
            // Create config file
            Path configPath = createConfigFile(proxy, localPort);

            // Start xray process
            ProcessBuilder pb = new ProcessBuilder(v2rayExecutable, "run", "-c", configPath.toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            runningProcesses.put(proxy.getId(), process);
            proxyRepository.save(proxy);

            log.info("Started V2Ray client for proxy {} on port {}", proxy.getId(), localPort);

            // Wait a bit for startup
            Thread.sleep(500);

            if (!process.isAlive()) {
                throw new RuntimeException("V2Ray process died immediately");
            }

            return localPort;

        } catch (Exception e) {
            usedPorts.remove(localPort);
            log.error("Failed to start V2Ray client for proxy {}: {}", proxy.getId(), e.getMessage());
            throw new RuntimeException("Failed to start V2Ray client: " + e.getMessage(), e);
        }
    }

    /**
     * Stop a V2Ray client for the given proxy
     */
    public void stopProxy(UUID proxyId) {
        Process process = runningProcesses.remove(proxyId);
        if (process != null) {
            process.destroy();
            log.info("Stopped V2Ray client for proxy {}", proxyId);

            Proxy proxy = proxyRepository.findById(proxyId).orElse(null);
            if (proxy != null && proxy.getLocalPort() != null) {
                usedPorts.remove(proxy.getLocalPort());
            }
        }
    }

    /**
     * Stop all running V2Ray clients
     */
    @PreDestroy
    public void stopAll() {
        log.info("Stopping all V2Ray clients...");
        for (UUID proxyId : new ArrayList<>(runningProcesses.keySet())) {
            stopProxy(proxyId);
        }
    }

    /**
     * Check if a proxy's V2Ray client is running
     */
    public boolean isRunning(UUID proxyId) {
        Process process = runningProcesses.get(proxyId);
        return process != null && process.isAlive();
    }

    /**
     * Get list of running proxy IDs
     */
    public Set<UUID> getRunningProxies() {
        return new HashSet<>(runningProcesses.keySet());
    }

    private int allocatePort() {
        int port;
        do {
            port = basePort + portCounter.getAndIncrement();
        } while (usedPorts.contains(port));
        usedPorts.add(port);
        return port;
    }

    private Path createConfigFile(Proxy proxy, int localPort) throws IOException {
        Path dir = Path.of(configDir);
        Files.createDirectories(dir);

        Path configPath = dir.resolve("proxy-" + proxy.getId() + ".json");

        Map<String, Object> config = generateConfig(proxy, localPort);
        String json = gson.toJson(config);

        Files.writeString(configPath, json);
        return configPath;
    }

    private Map<String, Object> generateConfig(Proxy proxy, int localPort) {
        Map<String, Object> config = new LinkedHashMap<>();

        // Inbound (local SOCKS5 proxy)
        config.put("inbounds", List.of(Map.of(
                "tag", "socks-in",
                "port", localPort,
                "listen", "127.0.0.1",
                "protocol", "socks",
                "settings", Map.of(
                        "auth", "noauth",
                        "udp", true))));

        // Outbound (V2Ray protocol)
        config.put("outbounds", List.of(generateOutbound(proxy)));

        return config;
    }

    private Map<String, Object> generateOutbound(Proxy proxy) {
        Map<String, Object> outbound = new LinkedHashMap<>();
        outbound.put("tag", "proxy");

        switch (proxy.getProtocol()) {
            case VLESS -> {
                outbound.put("protocol", "vless");
                outbound.put("settings", Map.of(
                        "vnext", List.of(Map.of(
                                "address", proxy.getHost(),
                                "port", proxy.getPort(),
                                "users", List.of(Map.of(
                                        "id", proxy.getUuid(),
                                        "encryption", proxy.getEncryption() != null ? proxy.getEncryption() : "none",
                                        "flow", ""))))));
            }
            case VMESS -> {
                outbound.put("protocol", "vmess");
                outbound.put("settings", Map.of(
                        "vnext", List.of(Map.of(
                                "address", proxy.getHost(),
                                "port", proxy.getPort(),
                                "users", List.of(Map.of(
                                        "id", proxy.getUuid(),
                                        "alterId", proxy.getAlterId() != null ? proxy.getAlterId() : 0,
                                        "security",
                                        proxy.getEncryption() != null ? proxy.getEncryption() : "auto"))))));
            }
            case SHADOWSOCKS -> {
                outbound.put("protocol", "shadowsocks");
                outbound.put("settings", Map.of(
                        "servers", List.of(Map.of(
                                "address", proxy.getHost(),
                                "port", proxy.getPort(),
                                "method", proxy.getEncryption(),
                                "password", proxy.getPassword()))));
            }
            case TROJAN -> {
                outbound.put("protocol", "trojan");
                outbound.put("settings", Map.of(
                        "servers", List.of(Map.of(
                                "address", proxy.getHost(),
                                "port", proxy.getPort(),
                                "password", proxy.getPassword()))));
            }
            default -> throw new IllegalArgumentException("Unsupported protocol: " + proxy.getProtocol());
        }

        // Stream settings (transport + TLS)
        outbound.put("streamSettings", generateStreamSettings(proxy));

        return outbound;
    }

    private Map<String, Object> generateStreamSettings(Proxy proxy) {
        Map<String, Object> stream = new LinkedHashMap<>();

        // Transport
        String transport = proxy.getTransport() != null ? proxy.getTransport() : "tcp";
        stream.put("network", transport);

        // Transport-specific settings
        switch (transport) {
            case "ws" -> {
                Map<String, Object> wsSettings = new LinkedHashMap<>();
                if (proxy.getPath() != null)
                    wsSettings.put("path", proxy.getPath());
                if (proxy.getWsHost() != null) {
                    wsSettings.put("headers", Map.of("Host", proxy.getWsHost()));
                }
                stream.put("wsSettings", wsSettings);
            }
            case "grpc" -> {
                Map<String, Object> grpcSettings = new LinkedHashMap<>();
                if (proxy.getPath() != null)
                    grpcSettings.put("serviceName", proxy.getPath());
                stream.put("grpcSettings", grpcSettings);
            }
            case "tcp" -> {
                // Default, no extra config needed
            }
        }

        // TLS/Security
        String security = proxy.getSecurity() != null ? proxy.getSecurity() : "";
        if ("tls".equals(security)) {
            stream.put("security", "tls");
            Map<String, Object> tlsSettings = new LinkedHashMap<>();
            if (proxy.getSni() != null)
                tlsSettings.put("serverName", proxy.getSni());
            if (proxy.getAlpn() != null)
                tlsSettings.put("alpn", List.of(proxy.getAlpn().split(",")));
            if (proxy.getFingerprint() != null)
                tlsSettings.put("fingerprint", proxy.getFingerprint());
            stream.put("tlsSettings", tlsSettings);
        } else if ("reality".equals(security)) {
            stream.put("security", "reality");
            Map<String, Object> realitySettings = new LinkedHashMap<>();
            if (proxy.getSni() != null)
                realitySettings.put("serverName", proxy.getSni());
            if (proxy.getPublicKey() != null)
                realitySettings.put("publicKey", proxy.getPublicKey());
            if (proxy.getShortId() != null)
                realitySettings.put("shortId", proxy.getShortId());
            if (proxy.getFingerprint() != null)
                realitySettings.put("fingerprint", proxy.getFingerprint());
            stream.put("realitySettings", realitySettings);
        }

        return stream;
    }
}
