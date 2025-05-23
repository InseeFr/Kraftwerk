package cucumber.functional_tests.config;

import fr.insee.kraftwerk.KraftwerkApi;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(classes = KraftwerkApi.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-cucumber")
@ComponentScan(basePackages = {
        "fr.insee.kraftwerk.core",
        "fr.insee.kraftwerk.encryption",
        "stubs"
})
@EnableAutoConfiguration
public class CucumberSpringConfiguration {
}
