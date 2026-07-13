package ir.netpick.platform.mailmine.dto;

import ir.netpick.platform.core.enums.ProxyProtocol;

public record ProxyRequest(
        ProxyProtocol protocol,
        String host,
        Integer port,
        String username,
        String password,
        String description,
        String vercelToken) {
}









