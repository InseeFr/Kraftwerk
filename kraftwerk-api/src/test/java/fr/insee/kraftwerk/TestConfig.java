package fr.insee.kraftwerk;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    // We need this class to make @Value in TestUtils work
    @Bean
    public TestUtils testUtils() {
        return new TestUtils();
    }
}
