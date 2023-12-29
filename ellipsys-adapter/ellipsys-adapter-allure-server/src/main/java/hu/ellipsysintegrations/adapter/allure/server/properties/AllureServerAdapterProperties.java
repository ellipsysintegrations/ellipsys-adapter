package hu.ellipsysintegrations.adapter.allure.server.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
@Accessors(fluent = true)
public class AllureServerAdapterProperties {

    @Setter
    private Integer mappedPort = 0;

    @Value("${allure.server.protocol}")
    private String protocol;

    @Value("${allure.server.host}")
    private String host;

    @Value("${allure.server.port}")
    private int port;

    @Value("${allure.server.image.name.prefix}")
    private String imageNamePrefix;

    @Value("${allure.server.path}")
    private String path;

    @Value("${allure.server.service.name}")
    private String serviceName;

    @Value("${allure.server.image.name}")
    private String imageName;

    @Value("${allure.server.ui.port}")
    private int uiPort;

    @Value("${allure.server.ui.path}")
    private String uiPath;

    @Value("${allure.server.ui.service.name}")
    private String uiServiceName;

    @Value("${allure.server.ui.image.name}")
    private String uiImageName;

    @Value("${allure.server.defaultProjectId}")
    private String defaultProjectId;

    @Value("${allure.server.defaultFolder}")
    private String defaultFolder;

    @Value("${allure.report.url.system.key}")
    private String allureReportUrlSystemKey;

}
