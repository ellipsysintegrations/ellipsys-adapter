package hu.ellipsysintegrations.adapter.allure.server.extension;

import com.codeborne.selenide.Configuration;
import hu.ellipsysintegrations.adapter.allure.server.properties.SelenideProperties;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@NoArgsConstructor
public class SelenideExtension implements BeforeAllCallback {
    private static final Logger log = LogManager.getLogger(SelenideExtension.class);

    @SneakyThrows
    public void beforeAll(ExtensionContext extensionContext) {
        ApplicationContext springContext = SpringExtension.getApplicationContext(extensionContext);
        SelenideProperties selenideConfiguration = springContext.getBean(SelenideProperties.class);
        if (Boolean.TRUE.equals(selenideConfiguration.headless())) {
            Configuration.headless = selenideConfiguration.headless();
            log.debug("[Selenide] headless mode: {}", selenideConfiguration.headless());
        }

        Configuration.reportsFolder = selenideConfiguration.reportsFolder();
        Configuration.savePageSource = selenideConfiguration.savePageSource();
        Configuration.pageLoadTimeout = 90000L;
        log.debug("[Selenide] reportsFolder: {}", selenideConfiguration.reportsFolder());
        log.debug("[Selenide] savePageSource: {}", selenideConfiguration.savePageSource());
    }
}
