package ir.netpick.mailmine.scrape.mapper;

import ir.netpick.mailmine.scrape.dto.ProxyResponse;
import ir.netpick.mailmine.scrape.model.Proxy;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ProxyDTOMapper implements Function<Proxy, ProxyResponse> {

    @Override
    public ProxyResponse apply(Proxy proxy) {
        return new ProxyResponse(
                proxy.getId(),
                proxy.getProtocol(),
                proxy.getHost(),
                proxy.getPort(),
                proxy.getUsername(),
                proxy.getStatus(),
                proxy.getLastTestedAt(),
                proxy.getLastUsedAt(),
                proxy.getSuccessCount(),
                proxy.getFailureCount(),
                proxy.getAvgResponseTimeMs(),
                proxy.getDescription(),
                proxy.getCreatedAt(),
                // V2Ray specific fields
                proxy.getUuid(),
                proxy.getEncryption(),
                proxy.getTransport(),
                proxy.getSecurity(),
                proxy.getSni(),
                proxy.getLocalPort(),
                proxy.isV2RayProtocol());
    }
}
