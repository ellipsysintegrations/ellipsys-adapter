package hu.ellipsysintegrations;

import hu.ellipsysintegrations.adapter.allure.server.cli.CliRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication(proxyBeanMethods = false)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class AllureServerAdapterApplication implements CommandLineRunner {

    @Autowired
    private CliRunner cliRunner;

    public static void main(String[] args) {
        new SpringApplicationBuilder(AllureServerAdapterApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        cliRunner.run(args);
    }

}
