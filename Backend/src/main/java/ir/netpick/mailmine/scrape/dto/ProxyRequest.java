package ir.netpick.mailmine.scrape.dto;

import ir.netpick.mailmine.common.enums.ProxyProtocol;

public record ProxyRequest(
        ProxyProtocol protocol,
        String host,
        Integer port,
        String username,
        String password,
        String description) {
}
