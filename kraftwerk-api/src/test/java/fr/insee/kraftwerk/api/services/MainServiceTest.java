package fr.insee.kraftwerk.api.services;

import fr.insee.kraftwerk.TestConfig;
import fr.insee.kraftwerk.TestUtils;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.api.process.MainProcessingGenesis;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
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
@AutoConfigureMockMvc
@Import(TestConfig.class)
class MainServiceTest {

    @Autowired
    private MockMvc mockMvc; // Simulates HTTP requests to the REST endpoints

    // Constants for user roles
    private static final String USER_KRAFTWERK = "USER";
    private static final String ADMIN = "ADMIN";

    // API endpoints under test
    private static final String URI_MAIN = "/main";
    private static final String URI_MAIN_LUNATIC = "/main/lunatic-only";
    private static final String URI_MAIN_GENESIS = "/main/genesis";
    private static final String URI_MAIN_GENESIS_LUNATIC = "/main/genesis/lunatic-only";
    private static final String URI_MAIN_FILE_BY_FILE = "/main/file-by-file";

    // Mock beans to replace actual service implementations
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private MainProcessingGenesis mainProcessingGenesis;
    @MockitoBean
    private MainProcessing mainProcessing;
    @MockitoBean
    private MainService mainService;

    @Autowired
    private TestUtils testUtils;

    /**
     * Provides a stream of URIs for the non-genesis endpoints.
     */
    private static Stream<Arguments> endpointsMain(){
        return Stream.of(
                Arguments.of(URI_MAIN),
                Arguments.of(URI_MAIN_LUNATIC),
                Arguments.of(URI_MAIN_FILE_BY_FILE)
        );
    }

    /**
     * Provides a stream of URIs for the genesis-related endpoints.
     */
    private static Stream<Arguments> endpointsMainGenesis(){
        return Stream.of(
                Arguments.of(URI_MAIN_GENESIS),
                Arguments.of(URI_MAIN_GENESIS_LUNATIC )
        );
    }

    /**
     * Tests that users with the "USER" role can access the main service endpoints.
     */
    @ParameterizedTest
    @MethodSource("endpointsMain")
    @DisplayName("Kraftwerk users should access main services")
    void kraftwerk_users_should_access_main_services(String endpointURI) throws Exception{
        doNothing().when(mainProcessing).runMain();
        Jwt jwt = testUtils.generateJwt(List.of("USER"), USER_KRAFTWERK);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(put(endpointURI).header("Authorization", "bearer token_blabla")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("/src/test/resources/data/in/SAMPLETEST-SIMPLE-RESPONSE"))
                .andExpect(status().isOk());
    }

    /**
     * Tests that users with the "USER" role can access genesis-related endpoints.
     */
    @ParameterizedTest
    @MethodSource("endpointsMainGenesis")
    @DisplayName("Kraftwerk users should access main services with Genesis")
    void kraftwerk_users_should_access_main_services_with_genesis(String endpointURI) throws Exception{
        doNothing().when(mainProcessingGenesis).runMain(anyString());
        Jwt jwt = testUtils.generateJwt(List.of("USER"), USER_KRAFTWERK);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(put(endpointURI).header("Authorization", "bearer token_blabla")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Test"))
                .andExpect(status().isOk());
    }

    /**
     * Tests that admins can access all main service endpoints.
     */
    @ParameterizedTest
    @MethodSource("endpointsMain")
    @DisplayName("Admins should access main services")
    void admins_should_access_main_services(String endpointURI) throws Exception{
        doNothing().when(mainProcessing).runMain();
        Jwt jwt = testUtils.generateJwt(List.of("ADMIN"), ADMIN);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(put(endpointURI).header("Authorization", "bearer token_blabla")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Test"))
                .andExpect(status().isOk());
    }

    /**
     * Tests that admins can access all genesis-related endpoints.
     */
    @ParameterizedTest
    @MethodSource("endpointsMainGenesis")
    @DisplayName("Admins should access main services with genesis")
    void admins_should_access_main_services_with_genesis(String endpointURI) throws Exception{
        doNothing().when(mainProcessingGenesis).runMain(anyString());
        Jwt jwt = testUtils.generateJwt(List.of("ADMIN"), ADMIN);
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(put(endpointURI).header("Authorization", "bearer token_blabla")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Test"))
                .andExpect(status().isOk());
    }

    /**
     * Tests that users with incorrect roles cannot access main service endpoints.
     */
    @ParameterizedTest
    @MethodSource("endpointsMain")
    @DisplayName("Incorrect roles should not access main services")
    void no_correct_roles_should_not_access_main_services(String endpointURI) throws Exception{
        doNothing().when(mainProcessing).runMain();
        Jwt jwt = testUtils.generateJwt(List.of(""), "Not_a_good_role");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(put(endpointURI).header("Authorization", "bearer token_blabla")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("\\src\\test\\resources\\data\\in\\SAMPLETEST-SIMPLE-RESPONSE"))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that users with incorrect roles cannot access genesis-related endpoints.
     */
    @ParameterizedTest
    @MethodSource("endpointsMainGenesis")
    @DisplayName("Incorrect roles should not access main services with Genesis")
    void no_correct_roles_should_not_access_main_services_with_genesis(String endpointURI) throws Exception{
        doNothing().when(mainProcessingGenesis).runMain(anyString());
        Jwt jwt = testUtils.generateJwt(List.of(""), "Not_a_good_role");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(put(endpointURI).header("Authorization", "bearer token_blabla")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Test"))
                .andExpect(status().isForbidden());
    }

}