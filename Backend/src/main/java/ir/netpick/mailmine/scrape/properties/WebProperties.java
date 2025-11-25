package ir.netpick.mailmine.scrape.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Configuration
@Component
@ConfigurationProperties(prefix = "web")
public class WebProperties {
    private String chromeLocation;
    private String driverLocation;

}
