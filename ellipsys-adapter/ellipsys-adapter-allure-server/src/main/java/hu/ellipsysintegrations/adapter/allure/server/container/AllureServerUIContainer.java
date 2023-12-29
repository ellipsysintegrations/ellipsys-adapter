package hu.ellipsysintegrations.adapter.allure.server.container;

import hu.ellipsysintegrations.adapter.allure.server.properties.AllureServerAdapterProperties;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class AllureServerUIContainer<SELF extends AllureServerUIContainer<SELF>> extends GenericContainer<SELF> {

    private AllureServerAdapterProperties properties;

    public AllureServerUIContainer(DockerImageName dockerImageName, AllureServerAdapterProperties properties) {
        super(dockerImageName);
        this.properties = properties;
        DockerImageName baseImageName = DockerImageName.parse(properties.imageNamePrefix() + "/" + properties.uiServiceName());
        dockerImageName.assertCompatibleWith(baseImageName);
        this.waitStrategy = (new HttpWaitStrategy()).forPath(properties.uiPath());
        this.addExposedPort(properties.uiPort());
    }

    @Override
    protected void configure() {
        this.addEnv("ALLURE_DOCKER_PUBLIC_API_URL_PREFIX", "");
        this.addEnv("ALLURE_DOCKER_PUBLIC_API_URL", String.format("%s://%s:%d", properties.protocol(), properties.host(), properties.mappedPort()));
    }

}

