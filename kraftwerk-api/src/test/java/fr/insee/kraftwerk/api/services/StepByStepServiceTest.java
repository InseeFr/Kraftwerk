package fr.insee.kraftwerk.api.services;

import fr.insee.kraftwerk.TestConfig;
import fr.insee.kraftwerk.TestUtils;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.api.process.MainProcessingGenesisLegacy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles({"test", "ci-public"})
@Import(TestConfig.class)
@ComponentScan(basePackages = {"fr.insee.kraftwerk.core.encryption"})
@AutoConfigureMockMvc
class StepByStepServiceTest {

    @Autowired
    private MockMvc mockMvc; // Simulates HTTP requests to the REST endpoints

    // Constants for user roles
    private static final String USER_KRAFTWERK = "USER";
    private static final String ADMIN = "ADMIN";

    // API endpoints under test
    private static final String URI_STEPS = "/steps";
    private static final String URI_STEPS_WRITE_OUTPUT = URI_STEPS + "/writeOutputFiles";
    private static final String URI_STEPS_UNIMODAL = URI_STEPS + "/unimodalProcessing?dataMode=WEB";
    private static final String URI_STEPS_MULTIMODAL = URI_STEPS + "/multimodalProcessing";
    private static final String URI_STEPS_BUILD_VTL_BINDINGS = URI_STEPS + "/buildVtlBindings";
    private static final String URI_STEPS_BUILD_VTL_BINDINGS_DATAMODE = URI_STEPS + "/buildVtlBindings/WEB";
    private static final String URI_ARCHIVE = URI_STEPS + "/archive";

    // Mock beans to replace actual service implementations
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private MainProcessingGenesisLegacy mainProcessingGenesisLegacy;
    @MockitoBean
    private MainProcessing mainProcessing;
    @MockitoBean
    private StepByStepService stepByStepService;
    @Autowired
    private TestUtils testUtils;

    /**
     * Provides a stream of URIs for the steps endpoints.
     */
    private static Stream<Arguments> endpointsSteps(){
        return Stream.of(
                Arguments.of(URI_STEPS_WRITE_OUTPUT),
                Arguments.of(URI_STEPS_UNIMODAL),
                Arguments.of(URI_STEPS_MULTIMODAL),
                Arguments.of(URI_STEPS_BUILD_VTL_BINDINGS),
                Arguments.of(URI_STEPS_BUILD_VTL_BINDINGS_DATAMODE),
                Arguments.of(URI_ARCHIVE)
        );
    }

    /**
     * Tests that users with the "USER" role can't access the steps endpoints.
     */
    @ParameterizedTest
    @MethodSource("endpointsSteps")
    @DisplayName("Kraftwerk users should not access steps services")
    void kraftwerk_users_should_not_access_steps_services(String endpointURI) throws Exception{
        doNothing().when(mainProcessing).runMain();
        Jwt jwt = testUtils.generateJwt(List.of("USER"), USER_KRAFTWERK);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(put(endpointURI).header("Authorization", "bearer token_blabla")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("/src/test/resources/data/in/SAMPLETEST-SIMPLE-RESPONSE"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("endpointsSteps")
    @DisplayName("Admins should access steps services")
    void admins_should_access_steps_services(String endpointURI) throws Exception{
        doNothing().when(mainProcessing).runMain();
        Jwt jwt = testUtils.generateJwt(List.of("ADMIN"), ADMIN);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(put(endpointURI).header("Authorization", "bearer token_blabla")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("/src/test/resources/data/in/SAMPLETEST-SIMPLE-RESPONSE"))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("endpointsSteps")
    @DisplayName("Incorrect roles should not access steps services")
    void incorrect_roles_should_not_access_steps_services(String endpointURI) throws Exception{
        doNothing().when(mainProcessing).runMain();
        Jwt jwt = testUtils.generateJwt(List.of(""), "Test no roles");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(put(endpointURI).header("Authorization", "bearer token_blabla")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("/src/test/resources/data/in/SAMPLETEST-SIMPLE-RESPONSE"))
                .andExpect(status().isForbidden());
    }
}