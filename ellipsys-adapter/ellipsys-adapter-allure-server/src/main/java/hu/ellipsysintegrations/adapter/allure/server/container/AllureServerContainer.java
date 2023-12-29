package hu.ellipsysintegrations.adapter.allure.server.container;

import hu.ellipsysintegrations.adapter.allure.server.properties.AllureServerAdapterProperties;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class AllureServerContainer<SELF extends AllureServerContainer<SELF>> extends GenericContainer<SELF> {

    public AllureServerContainer(DockerImageName dockerImageName, AllureServerAdapterProperties properties) {
        super(dockerImageName);
        DockerImageName baseImageName = DockerImageName.parse(properties.imageNamePrefix() + "/" + properties.serviceName());
        dockerImageName.assertCompatibleWith(baseImageName);
        this.waitStrategy = (new HttpWaitStrategy()).forPath(properties.path()).forStatusCode(200);
        this.addExposedPort(properties.port());
    }

    @Override
    protected void configure() {
        this.addEnv("CHECK_RESULTS_EVERY_SECONDS", "NONE");
        this.addEnv("KEEP_HISTORY", "1");
        this.addEnv("KEEP_HISTORY_LATEST", "10");
    }

}

