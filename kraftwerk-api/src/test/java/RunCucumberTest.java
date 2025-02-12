import fr.insee.kraftwerk.api.KraftwerkApi;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty"},
                glue = "cucumber")
@SpringBootTest
@ContextConfiguration(classes = KraftwerkApi.class)
public class RunCucumberTest {
}