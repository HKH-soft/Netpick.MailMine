package ir.netpick.platform.gatekeeper.service;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "ir.netpick.platform.gatekeeper",
    "ir.netpick.platform.taskfarm",
    "ir.netpick.platform.core",
    "ir.netpick.platform.init"
})
@EntityScan("ir.netpick.platform")
@EnableJpaRepositories("ir.netpick.platform")
class TestApplication {
}
