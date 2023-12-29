package hu.ellipsysintegrations.adapter.allure.server;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import hu.ellipsysintegrations.adapter.allure.server.api.client.AllureServerClient;
import hu.ellipsysintegrations.adapter.allure.server.api.dto.SendResultsRequestDto;
import hu.ellipsysintegrations.adapter.allure.server.cli.CliRunner;
import hu.ellipsysintegrations.adapter.allure.server.container.AllureServerContainer;
import hu.ellipsysintegrations.adapter.allure.server.container.AllureServerUIContainer;
import hu.ellipsysintegrations.adapter.allure.server.extension.SelenideExtension;
import hu.ellipsysintegrations.adapter.allure.server.properties.AllureServerAdapterProperties;
import hu.ellipsysintegrations.adapter.allure.server.properties.SelenideProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@ExtendWith(SpringExtension.class)
@ExtendWith(SelenideExtension.class)
@TestPropertySource(locations = "classpath:application.properties")
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AllureServerContainerTests {

    @BeforeAll
    public void init() throws Exception {
    }

    @AfterAll
    public void close() throws Exception {
    }

    @Autowired
    private CliRunner cliRunner;

    @Autowired
    private AllureServerClient allureServerClient;

    @Autowired
    private AllureServerAdapterProperties properties;

    @Test
    @Order(1)
    @SneakyThrows
    public void uploadFakeResultsToContainer() {

        //Start backend container
        AllureServerContainer<?> allureServerContainer = new AllureServerContainer<>(
                DockerImageName.parse(properties.imageName()), properties)
                .withNetwork(Network.SHARED);
        allureServerContainer.start();

        //Set mappedPort before UI container init
        int mappedPort = allureServerContainer.getMappedPort(properties.port());
        properties.mappedPort(mappedPort);

        //Start frontend container
        AllureServerUIContainer<?> allureServerUIContainer = new AllureServerUIContainer<>(
                DockerImageName.parse(properties.uiImageName()), properties)
                .withNetwork(Network.SHARED);
        allureServerUIContainer.start();

        //Send fake results via Rest API
        SendResultsRequestDto requestDto = cliRunner.requestDtoBuilder(properties.defaultFolder()).build();
        allureServerClient.postSendResults(properties.defaultProjectId(), requestDto);
        allureServerClient.getGenerateReport(properties.defaultProjectId().toLowerCase());

        String reportUrl = String.format("%s://%s:%d/%s/projects/%s/reports/latest/index.html?redirect=false#behaviors",
                properties.protocol(), properties.host(), mappedPort, properties.serviceName(), properties.defaultProjectId());
        log.info("Report url: {}", reportUrl);

        //Verify Allure results
        Selenide.open(reportUrl);
        ElementsCollection nodeLeafs = Selenide.$$(By.className("node__leaf"));

        Assertions.assertThat(nodeLeafs.size()).isNotZero();
        Assertions.assertThat(nodeLeafs.size()).isEqualTo(2);

        Assertions.assertThat(nodeLeafs.first().text()).contains("dummyTest()").contains("48ms");
        Assertions.assertThat(nodeLeafs.last().text()).contains("getInfluxDBClient()").contains("3m 23s");

    }

    @TestConfiguration
    static class AllureServerContainerConfiguration {
        @Bean
        AllureServerAdapterProperties allureServerAdapterProperties() {
            return new AllureServerAdapterProperties();
        }

        @Bean
        AllureServerClient allureServerClient() {
            return new AllureServerClient();
        }

        @Bean
        CliRunner cliRunner() {
            return new CliRunner();
        }

        @Bean
        SelenideProperties selenideProperties() {
            return new SelenideProperties();
        }
    }

}
