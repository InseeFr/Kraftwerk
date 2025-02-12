package fr.insee.kraftwerk.api.services;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.configuration.security.OIDCAuthConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MainService.class,
        properties = {"fr.insee.kraftwerk.authentication=OIDC",
                "fr.insee.kraftwerk.security.whitelist-matchers=/v3/api-docs/**,/swagger-ui/**,/swagger-ui.html,/actuator/**,/error,/,/health-check/**"})
@Import({OIDCAuthConfiguration.class,
        ConfigProperties.class,
        MinioConfig.class})
class MainServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void userKraftwerkShouldAccessMainServices() throws Exception {
        mockMvc.perform(put("/main")
                .contentType(MediaType.TEXT_PLAIN)
                .content("src/test/resources/data/in/SAMPLETEST-SIMPLE-RESPONSE")
                .param("archiveAtEnd", "false")
                .param("withAllReportingData", "false")
                .with(jwt().jwt(builder -> builder.claim("realm_access",
                        Map.of("roles", List.of("USER_KRAFTWERK"))))))
                .andExpect(status().isOk());
        mockMvc.perform(put("/main/lunatic-only")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("src/test/resources/data/in/SAMPLETEST-SIMPLE-RESPONSE")
                        .param("archiveAtEnd", "false")
                        .with(jwt().jwt(builder -> builder.claim("realm_access",
                                Map.of("roles", List.of("USER_KRAFTWERK"))))))
                .andExpect(status().isOk());
    }

}