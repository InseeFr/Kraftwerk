package fr.insee.kraftwerk;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@TestConfiguration
@EnableAsync
public class TestConfig {

    // We need this class to make @Value in TestUtils work
    @Bean
    public TestUtils testUtils() {
        return new TestUtils();
    }

    @Bean(name = "kraftwerkExecutor")
    public Executor kraftwerkExecutor() {
        return Runnable::run;
    }
}
