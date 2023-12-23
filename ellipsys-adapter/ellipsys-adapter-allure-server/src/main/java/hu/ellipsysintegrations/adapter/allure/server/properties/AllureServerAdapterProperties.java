package hu.ellipsysintegrations.adapter.allure.server.properties;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
@Accessors(fluent = true)
public class AllureServerAdapterProperties {

    @Value("${allure.server.protocol}")
    private String protocol;

    @Value("${allure.server.host}")
    private String host;

    @Value("${allure.server.port}")
    private String port;

    @Value("${allure.server.defaultFolder}")
    private String defaultFolder;

    @Value("${allure.report.url.system.key}")
    private String allureReportUrlSystemKey;

}
