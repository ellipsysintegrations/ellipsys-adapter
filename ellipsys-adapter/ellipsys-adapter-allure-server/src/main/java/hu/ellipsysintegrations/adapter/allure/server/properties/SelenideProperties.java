package hu.ellipsysintegrations.adapter.allure.server.properties;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Component
public class SelenideProperties {
    @Value("${selenide.headless}")
    private Boolean headless;
    @Value("${selenide.wdmProxy}")
    private String wdmProxy;
    @Value("${selenide.reportsFolder}")
    private String reportsFolder;
    @Value("${selenide.savePageSource}")
    private Boolean savePageSource;

    public Boolean headless() {
        return this.headless;
    }

    public String wdmProxy() {
        return this.wdmProxy;
    }

    public String reportsFolder() {
        return this.reportsFolder;
    }

    public Boolean savePageSource() {
        return this.savePageSource;
    }
}
