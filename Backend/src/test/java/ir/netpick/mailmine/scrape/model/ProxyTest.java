package ir.netpick.mailmine.scrape.model;

import ir.netpick.mailmine.common.enums.ProxyProtocol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProxyTest {

    @Test
    void testParseShadowsocksWithSpaceComment() {
        // Real-world messy format: ss://base64@host:port # [ Comment ] 🔒
        String messyUrl = "ss://Y2hhY2hhMjAtaWV0Zi1wb2x5MTMwNTo1MXlsb1FDOEQ5dzFXYWU3Rkh0STY1@135.225.124.80:48172 # [ By EbraSha ] 🔒";

        Proxy proxy = Proxy.fromUrl(messyUrl);

        assertEquals(ProxyProtocol.SHADOWSOCKS, proxy.getProtocol());
        assertEquals("135.225.124.80", proxy.getHost());
        assertEquals(48172, proxy.getPort());
        assertEquals("chacha20-ietf-poly1305", proxy.getEncryption());
        assertEquals("51yloQC8D9w1Wae7FHtI65", proxy.getPassword());
        assertNotNull(proxy.getDescription());
    }

    @Test
    void testParseShadowsocksWithHashComment() {
        // Standard format with # name
        String url = "ss://Y2hhY2hhMjAtaWV0Zi1wb2x5MTMwNTo1MXlsb1FDOEQ5dzFXYWU3Rkh0STY1@135.225.124.80:48172#MyServer";

        Proxy proxy = Proxy.fromUrl(url);

        assertEquals(ProxyProtocol.SHADOWSOCKS, proxy.getProtocol());
        assertEquals("135.225.124.80", proxy.getHost());
        assertEquals(48172, proxy.getPort());
        assertEquals("MyServer", proxy.getDescription());
    }

    @Test
    void testParseShadowsocksClean() {
        // Clean format without any comment
        String url = "ss://Y2hhY2hhMjAtaWV0Zi1wb2x5MTMwNTo1MXlsb1FDOEQ5dzFXYWU3Rkh0STY1@135.225.124.80:48172";

        Proxy proxy = Proxy.fromUrl(url);

        assertEquals(ProxyProtocol.SHADOWSOCKS, proxy.getProtocol());
        assertEquals("135.225.124.80", proxy.getHost());
        assertEquals(48172, proxy.getPort());
    }

    @Test
    void testParseVlessWithSpaceComment() {
        String messyUrl = "vless://a1b2c3d4-5678-90ab-cdef-123456789abc@example.com:443?security=tls&type=tcp # Some Comment 🔐";

        Proxy proxy = Proxy.fromUrl(messyUrl);

        assertEquals(ProxyProtocol.VLESS, proxy.getProtocol());
        assertEquals("example.com", proxy.getHost());
        assertEquals(443, proxy.getPort());
        assertEquals("a1b2c3d4-5678-90ab-cdef-123456789abc", proxy.getUuid());
    }

    @Test
    void testParseTrojanWithSpaceComment() {
        String messyUrl = "trojan://mypassword@server.com:443?sni=server.com # [ Premium ] ⚡";

        Proxy proxy = Proxy.fromUrl(messyUrl);

        assertEquals(ProxyProtocol.TROJAN, proxy.getProtocol());
        assertEquals("server.com", proxy.getHost());
        assertEquals(443, proxy.getPort());
        assertEquals("mypassword", proxy.getPassword());
    }

    @Test
    void testParseSocks5() {
        String url = "socks5://192.168.1.1:1080";

        Proxy proxy = Proxy.fromUrl(url);

        assertEquals(ProxyProtocol.SOCKS5, proxy.getProtocol());
        assertEquals("192.168.1.1", proxy.getHost());
        assertEquals(1080, proxy.getPort());
    }

    @Test
    void testParseSocks5WithAuth() {
        String url = "socks5://user:pass@192.168.1.1:1080";

        Proxy proxy = Proxy.fromUrl(url);

        assertEquals(ProxyProtocol.SOCKS5, proxy.getProtocol());
        assertEquals("192.168.1.1", proxy.getHost());
        assertEquals(1080, proxy.getPort());
        assertEquals("user", proxy.getUsername());
        assertEquals("pass", proxy.getPassword());
    }

    @Test
    void testParseSocks5WithSpaceComment() {
        String messyUrl = "socks5://192.168.1.1:1080 # Fast proxy";

        Proxy proxy = Proxy.fromUrl(messyUrl);

        assertEquals(ProxyProtocol.SOCKS5, proxy.getProtocol());
        assertEquals("192.168.1.1", proxy.getHost());
        assertEquals(1080, proxy.getPort());
    }

    @Test
    void testParseHostPortOnly() {
        String url = "192.168.1.1:1080";

        Proxy proxy = Proxy.fromUrl(url);

        assertEquals(ProxyProtocol.SOCKS5, proxy.getProtocol()); // Default
        assertEquals("192.168.1.1", proxy.getHost());
        assertEquals(1080, proxy.getPort());
    }

    @Test
    void testIsV2RayProtocol() {
        Proxy vless = new Proxy(ProxyProtocol.VLESS, "host", 443);
        Proxy vmess = new Proxy(ProxyProtocol.VMESS, "host", 443);
        Proxy ss = new Proxy(ProxyProtocol.SHADOWSOCKS, "host", 443);
        Proxy trojan = new Proxy(ProxyProtocol.TROJAN, "host", 443);
        Proxy socks5 = new Proxy(ProxyProtocol.SOCKS5, "host", 1080);
        Proxy http = new Proxy(ProxyProtocol.HTTP, "host", 8080);

        assertTrue(vless.isV2RayProtocol());
        assertTrue(vmess.isV2RayProtocol());
        assertTrue(ss.isV2RayProtocol());
        assertTrue(trojan.isV2RayProtocol());
        assertFalse(socks5.isV2RayProtocol());
        assertFalse(http.isV2RayProtocol());
    }

    @Test
    void testToProxyUrlForSocks5() {
        Proxy proxy = new Proxy(ProxyProtocol.SOCKS5, "192.168.1.1", 1080);
        assertEquals("socks5://192.168.1.1:1080", proxy.toProxyUrl());
    }

    @Test
    void testToProxyUrlForSocks5WithAuth() {
        Proxy proxy = new Proxy(ProxyProtocol.SOCKS5, "192.168.1.1", 1080, "user", "pass");
        assertEquals("socks5://user:pass@192.168.1.1:1080", proxy.toProxyUrl());
    }

    @Test
    void testToProxyUrlForV2RayRequiresLocalPort() {
        Proxy proxy = new Proxy(ProxyProtocol.VLESS, "example.com", 443);
        // Without localPort set, should throw
        assertThrows(IllegalStateException.class, proxy::toProxyUrl);
    }

    @Test
    void testToProxyUrlForV2RayWithLocalPort() {
        Proxy proxy = new Proxy(ProxyProtocol.VLESS, "example.com", 443);
        proxy.setLocalPort(30000);
        assertEquals("socks5://127.0.0.1:30000", proxy.toProxyUrl());
    }
}
