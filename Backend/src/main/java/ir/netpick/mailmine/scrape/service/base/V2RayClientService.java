package ir.netpick.mailmine.scrape.service.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ir.netpick.mailmine.scrape.model.Proxy;
import ir.netpick.mailmine.scrape.repository.ProxyRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
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
public class V2RayClientService {

    private final ProxyRepository proxyRepository;
    private Gson gson;

    @Value("${v2ray.executable:xray}")
    private String v2rayExecutable;

    @Value("${v2ray.config-dir:./v2ray-configs}")
    private String configDir;

    @Value("${v2ray.base-port:20000}")
    private int basePort;

    // Running processes: proxyId -> Process
    private Map<UUID, Process> runningProcesses;

    // Config file paths for cleanup
    private Map<UUID, Path> configFiles;

    // Port allocator
    private AtomicInteger portCounter;
    private Set<Integer> usedPorts;

    public V2RayClientService(ProxyRepository proxyRepository) {
        this.proxyRepository = proxyRepository;
    }

    @PostConstruct
    void init() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.runningProcesses = new ConcurrentHashMap<>();
        this.configFiles = new ConcurrentHashMap<>();
        this.portCounter = new AtomicInteger(0);
        this.usedPorts = ConcurrentHashMap.newKeySet();
        log.info("V2RayClientService initialized. Executable: {}, Config dir: {}, Base port: {}",
                v2rayExecutable, configDir, basePort);
    }

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
            configFiles.put(proxy.getId(), configPath);

            // Verify xray executable exists
            Path execPath = Path.of(v2rayExecutable);
            if (!Files.exists(execPath)) {
                throw new RuntimeException("V2Ray executable not found: " + v2rayExecutable);
            }

            log.info("Starting xray with config: {}", configPath);
            log.info("Command: {} run -c {}", v2rayExecutable, configPath);

            // Start xray process
            ProcessBuilder pb = new ProcessBuilder(v2rayExecutable, "run", "-c", configPath.toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            log.info("Xray process started (PID: {}), waiting for initialization...", process.pid());

            // Capture and log process output in background thread
            startOutputReader(proxy.getId(), process);

            runningProcesses.put(proxy.getId(), process);
            proxyRepository.save(proxy);

            log.info("Started V2Ray client for proxy {} on port {}", proxy.getId(), localPort);

            // Wait for startup and verify process is running
            Thread.sleep(1000);

            if (!process.isAlive()) {
                runningProcesses.remove(proxy.getId());
                configFiles.remove(proxy.getId());
                throw new RuntimeException("V2Ray process died immediately. Check logs for details.");
            }

            // Verify port is actually listening
            if (!waitForPort(localPort, 5000)) {
                log.error("V2Ray started but port {} is not listening after 5 seconds", localPort);
                stopProxy(proxy.getId());
                throw new RuntimeException("V2Ray process started but port is not listening");
            }

            log.info("V2Ray proxy {} verified listening on port {}", proxy.getId(), localPort);
            return localPort;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            usedPorts.remove(localPort);
            throw new RuntimeException("Interrupted while starting V2Ray client", e);
        } catch (Exception e) {
            usedPorts.remove(localPort);
            log.error("Failed to start V2Ray client for proxy {}: {}", proxy.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to start V2Ray client: " + e.getMessage(), e);
        }
    }

    /**
     * Start a background thread to read and log process output
     */
    private void startOutputReader(UUID proxyId, Process process) {
        Thread outputReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Log errors and warnings at appropriate levels
                    if (line.contains("error") || line.contains("Error") || line.contains("ERROR")) {
                        log.error("[xray-{}] {}", proxyId, line);
                    } else if (line.contains("warn") || line.contains("Warn") || line.contains("WARN")) {
                        log.warn("[xray-{}] {}", proxyId, line);
                    } else {
                        log.info("[xray-{}] {}", proxyId, line);
                    }
                }
            } catch (IOException e) {
                if (process.isAlive()) {
                    log.warn("Error reading xray output for proxy {}: {}", proxyId, e.getMessage());
                }
            }
        }, "xray-output-" + proxyId);
        outputReader.setDaemon(true);
        outputReader.start();
    }

    /**
     * Stop a V2Ray client for the given proxy
     */
    public void stopProxy(UUID proxyId) {
        Process process = runningProcesses.remove(proxyId);
        if (process != null) {
            // Try graceful shutdown first
            process.destroy();
            try {
                // Wait up to 3 seconds for graceful shutdown
                boolean exited = process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);
                if (!exited) {
                    log.warn("Proxy {} did not stop gracefully, forcing...", proxyId);
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
            log.info("Stopped V2Ray client for proxy {}", proxyId);
        }

        // Cleanup config file
        Path configPath = configFiles.remove(proxyId);
        if (configPath != null) {
            try {
                Files.deleteIfExists(configPath);
                log.debug("Deleted config file: {}", configPath);
            } catch (IOException e) {
                log.warn("Failed to delete config file {}: {}", configPath, e.getMessage());
            }
        }

        // Release port - check both DB and local tracking
        Proxy proxy = proxyRepository.findById(proxyId).orElse(null);
        if (proxy != null && proxy.getLocalPort() != null) {
            usedPorts.remove(proxy.getLocalPort());
            proxy.setLocalPort(null);
            proxyRepository.save(proxy);
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
        int attempts = 0;
        final int maxAttempts = 100;

        do {
            port = basePort + portCounter.getAndIncrement();
            attempts++;
            if (attempts > maxAttempts) {
                throw new RuntimeException(
                        "Could not find available port after " + maxAttempts + " attempts starting from " + basePort);
            }
        } while (usedPorts.contains(port) || !isPortAvailable(port));

        usedPorts.add(port);
        log.info("Allocated port {} for V2Ray proxy", port);
        return port;
    }

    /**
     * Check if a port is available on the system
     */
    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            log.debug("Port {} is not available: {}", port, e.getMessage());
            return false;
        }
    }

    /**
     * Wait for a port to become available (something is listening on it)
     */
    private boolean waitForPort(int port, int timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress("127.0.0.1", port), 500);
                return true; // Connection succeeded, port is listening
            } catch (IOException e) {
                // Port not ready yet, wait and retry
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
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
                                        "flow", proxy.getFlow() != null ? proxy.getFlow() : ""))))));
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
                if (proxy.getEncryption() == null || proxy.getPassword() == null) {
                    throw new IllegalArgumentException("Shadowsocks requires encryption method and password");
                }
                outbound.put("protocol", "shadowsocks");
                outbound.put("settings", Map.of(
                        "servers", List.of(Map.of(
                                "address", proxy.getHost(),
                                "port", proxy.getPort(),
                                "method", proxy.getEncryption(),
                                "password", proxy.getPassword()))));
            }
            case TROJAN -> {
                if (proxy.getPassword() == null) {
                    throw new IllegalArgumentException("Trojan requires password");
                }
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
            tlsSettings.put("allowInsecure", true); // Allow self-signed certs
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
