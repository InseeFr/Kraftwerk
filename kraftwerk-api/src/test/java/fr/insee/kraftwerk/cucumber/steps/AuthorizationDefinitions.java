package fr.insee.kraftwerk.cucumber.steps;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.configuration.security.NoAuthConfiguration;
import fr.insee.kraftwerk.api.configuration.security.OIDCAuthConfiguration;
import fr.insee.kraftwerk.api.services.MainService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = MainService.class,
        properties = {"fr.insee.kraftwerk.authentication=OIDC",
                "fr.insee.kraftwerk.security.whitelist-matchers=/v3/api-docs/**,/swagger-ui/**,/swagger-ui.html,/actuator/**,/error,/,/health-check/**"})
@Import({OIDCAuthConfiguration.class,
        NoAuthConfiguration.class,
        ConfigProperties.class,
        MinioConfig.class})
public class AuthorizationDefinitions {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions result;

    private String userRole;

    @Given("A user with the role {string}")
    public void aUserWithTheRole(String role){
        userRole = role;
    }

    @When("He sends a PUT request with main service with {string} input folder")
    public void gestionnaireShouldAccessStats(String folderName) throws Exception {
        mockMvc.perform(put("/main")
                .contentType(MediaType.TEXT_PLAIN)
                .content("src/test/resources/data/in/" + folderName)
                .param("archiveAtEnd", "false")
                .param("withAllReportingData", "false")
                .with(jwt().jwt(builder -> builder.claim("realm_access",
                        Map.of("roles", List.of(userRole))))));
    }

    @Then("Response should be {int}")
    public void responseShouldBeCode(int statusCode) throws Exception {
        result.andExpect(status().is(statusCode));
    }

}