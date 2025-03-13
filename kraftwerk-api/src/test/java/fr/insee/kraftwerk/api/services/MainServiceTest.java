package fr.insee.kraftwerk.api.services;

import fr.insee.kraftwerk.TestConfig;
import fr.insee.kraftwerk.TestUtils;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.api.process.MainProcessingGenesis;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
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


    ConfigProperties configProperties = new ConfigProperties();
    MinioConfig minioConfig;


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
        doNothing().when(mainProcessingGenesis).runMain(anyString(), anyInt());
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
        doNothing().when(mainProcessingGenesis).runMain(anyString(),anyInt());
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
        doNothing().when(mainProcessingGenesis).runMain(anyString(),anyInt());
        Jwt jwt = testUtils.generateJwt(List.of(""), "Not_a_good_role");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        mockMvc.perform(put(endpointURI).header("Authorization", "bearer token_blabla")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Test"))
                .andExpect(status().isForbidden());
    }

    /*** Main ***/

    @Test
    void testMain_Success() throws Exception {
        String inDirectory = "test-campaign-id";

        // Mock the dependencies
        FileUtilsInterface mockFileUtilsInterface = mock(FileUtilsInterface.class);
        MainProcessing mockMainProcessing = mock(MainProcessing.class);
        MainService mainService  = Mockito.spy(new MainService(configProperties,minioConfig));
        doReturn(mockFileUtilsInterface).when(mainService).getFileUtilsInterface();
        doReturn(mockMainProcessing).when(mainService).getMainProcessing(anyString(),anyBoolean(),anyBoolean(),anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doNothing().when(mockMainProcessing).runMain();

        // WHEN
        ResponseEntity<String> response = mainService.mainService(inDirectory, false, false, false);

        // THEN
        assertEquals(200, response.getStatusCode().value());
        assertEquals(inDirectory, response.getBody());
    }

    @Test
    void testMain_KraftwerkException() throws Exception {
        String inDirectory = "test-campaign-id";
        KraftwerkException exception = new KraftwerkException( HttpStatus.BAD_REQUEST.value(), "Kraftwerk error");

        // Mock the dependencies
        FileUtilsInterface mockFileUtilsInterface = mock(FileUtilsInterface.class);
        MainProcessing mockMainProcessing = mock(MainProcessing.class);
        MainService mainService  = Mockito.spy(new MainService(configProperties,minioConfig));
        doReturn(mockFileUtilsInterface).when(mainService).getFileUtilsInterface();
        doReturn(mockMainProcessing).when(mainService).getMainProcessing(anyString(),anyBoolean(),anyBoolean(),anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doThrow(exception).when(mockMainProcessing).runMain();

        // WHEN
        ResponseEntity<String> response = mainService.mainService(inDirectory, false, false, false);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Kraftwerk error", response.getBody());
    }


    /*** Main file by file ***/

    @Test
    void testMainFileByFile_Success() throws Exception {
        String inDirectory = "test-campaign-id";

        // Mock the dependencies
        FileUtilsInterface mockFileUtilsInterface = mock(FileUtilsInterface.class);
        MainProcessing mockMainProcessing = mock(MainProcessing.class);
        MainService mainService  = Mockito.spy(new MainService(configProperties,minioConfig));
        doReturn(mockFileUtilsInterface).when(mainService).getFileUtilsInterface();
        doReturn(mockMainProcessing).when(mainService).getMainProcessing(anyString(),anyBoolean(),anyBoolean(),anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doNothing().when(mockMainProcessing).runMain();

        // WHEN
        ResponseEntity<String> response = mainService.mainFileByFile(inDirectory, false, false);

        // THEN
        assertEquals(200, response.getStatusCode().value());
        assertEquals(inDirectory, response.getBody());
    }

    @Test
    void testMainFileByFile_KraftwerkException() throws Exception {
        String inDirectory = "test-campaign-id";
        KraftwerkException exception = new KraftwerkException( HttpStatus.BAD_REQUEST.value(), "Kraftwerk error");

        // Mock the dependencies
        FileUtilsInterface mockFileUtilsInterface = mock(FileUtilsInterface.class);
        MainProcessing mockMainProcessing = mock(MainProcessing.class);
        MainService mainService  = Mockito.spy(new MainService(configProperties,minioConfig));
        doReturn(mockFileUtilsInterface).when(mainService).getFileUtilsInterface();
        doReturn(mockMainProcessing).when(mainService).getMainProcessing(anyString(),anyBoolean(),anyBoolean(),anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doThrow(exception).when(mockMainProcessing).runMain();

        // WHEN
        ResponseEntity<String> response = mainService.mainFileByFile(inDirectory, false, false);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Kraftwerk error", response.getBody());
    }


    /*** Main Lunatic Only ***/

    @Test
    void testMainLunaticOnly_Success() throws Exception {
        String inDirectory = "test-campaign-id";

        // Mock the dependencies
        FileUtilsInterface mockFileUtilsInterface = mock(FileUtilsInterface.class);
        MainProcessing mockMainProcessing = mock(MainProcessing.class);
        MainService mainService  = Mockito.spy(new MainService(configProperties,minioConfig));
        doReturn(mockFileUtilsInterface).when(mainService).getFileUtilsInterface();
        doReturn(mockMainProcessing).when(mainService).getMainProcessing(anyString(),anyBoolean(),anyBoolean(),anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doNothing().when(mockMainProcessing).runMain();

        // WHEN
        ResponseEntity<String> response = mainService.mainLunaticOnly(inDirectory, false, false);

        // THEN
        assertEquals(200, response.getStatusCode().value());
        assertEquals(inDirectory, response.getBody());
    }

    @Test
    void testMainLunaticOnly_KraftwerkException() throws Exception {
        String inDirectory = "test-campaign-id";
        KraftwerkException exception = new KraftwerkException( HttpStatus.BAD_REQUEST.value(), "Kraftwerk error");

        // Mock the dependencies
        FileUtilsInterface mockFileUtilsInterface = mock(FileUtilsInterface.class);
        MainProcessing mockMainProcessing = mock(MainProcessing.class);
        MainService mainService  = Mockito.spy(new MainService(configProperties,minioConfig));
        doReturn(mockFileUtilsInterface).when(mainService).getFileUtilsInterface();
        doReturn(mockMainProcessing).when(mainService).getMainProcessing(anyString(),anyBoolean(),anyBoolean(),anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doThrow(exception).when(mockMainProcessing).runMain();

        // WHEN
        ResponseEntity<String> response = mainService.mainLunaticOnly(inDirectory, false, false);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Kraftwerk error", response.getBody());
    }


    /*** Main Genesis ***/

    @Test
    void testMainGenesis_Success() throws Exception {
        String idCampaign = "test-campaign-id";

        // Mock the dependencies
        FileUtilsInterface mockFileUtilsInterface = mock(FileUtilsInterface.class);
        MainProcessingGenesis mockMainProcessing = mock(MainProcessingGenesis.class);
        MainService mainService  = Mockito.spy(new MainService(configProperties,minioConfig));
        doReturn(mockFileUtilsInterface).when(mainService).getFileUtilsInterface();
        doReturn(mockMainProcessing).when(mainService).getMainProcessingGenesis(anyBoolean(), anyBoolean(),any(FileUtilsInterface.class));
        doNothing().when(mockMainProcessing).runMain(idCampaign,100);

        // WHEN
        ResponseEntity<String> response = mainService.mainGenesis(idCampaign,100, false);

        // THEN
        assertEquals(200, response.getStatusCode().value());
        assertEquals(idCampaign, response.getBody());
    }

    @Test
    void testMainGenesis_KraftwerkException() throws Exception {
        String idCampaign = "test-campaign-id";
        KraftwerkException exception = new KraftwerkException( HttpStatus.BAD_REQUEST.value(), "Kraftwerk error");


        // Mock the dependencies
        FileUtilsInterface mockFileUtilsInterface = mock(FileUtilsInterface.class);
        MainProcessingGenesis mockMainProcessing = mock(MainProcessingGenesis.class);
        MainService mainService  = Mockito.spy(new MainService(configProperties,minioConfig));
        doReturn(mockFileUtilsInterface).when(mainService).getFileUtilsInterface();
        doReturn(mockMainProcessing).when(mainService).getMainProcessingGenesis(anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doThrow(exception).when(mockMainProcessing).runMain(idCampaign,100);

        // WHEN
        ResponseEntity<String> response = mainService.mainGenesis(idCampaign,100, false);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Kraftwerk error", response.getBody());
    }

    @Test
    void testMainGenesis_IOException() throws Exception {
        String idCampaign = "test-campaign-id";
        IOException exception = new IOException("IO error");

        // Mock the dependencies
        FileUtilsInterface mockFileUtilsInterface = mock(FileUtilsInterface.class);
        MainProcessingGenesis mockMainProcessing = mock(MainProcessingGenesis.class);
        MainService mainService  = Mockito.spy(new MainService(configProperties,minioConfig));
        doReturn(mockFileUtilsInterface).when(mainService).getFileUtilsInterface();
        doReturn(mockMainProcessing).when(mainService).getMainProcessingGenesis(anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doThrow(exception).when(mockMainProcessing).runMain(idCampaign,100);

        // WHEN
        ResponseEntity<String> response = mainService.mainGenesis(idCampaign,100, false);

        // THEN
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("IO error", response.getBody());
    }

    /*** Main Genesis Lunatic Only***/

    @Test
    void testMainGenesisLunaticOnly_Success() throws Exception {
        String idCampaign = "test-campaign-id";

        // Mock the dependencies
        FileUtilsInterface mockFileUtilsInterface = mock(FileUtilsInterface.class);
        MainProcessingGenesis mockMainProcessing = mock(MainProcessingGenesis.class);
        MainService mainService  = Mockito.spy(new MainService(configProperties,minioConfig));
        doReturn(mockFileUtilsInterface).when(mainService).getFileUtilsInterface();
        doReturn(mockMainProcessing).when(mainService).getMainProcessingGenesis(anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doNothing().when(mockMainProcessing).runMain(idCampaign,100);

        // WHEN
        ResponseEntity<String> response = mainService.mainGenesisLunaticOnly(idCampaign,100, false);

        // THEN
        assertEquals(200, response.getStatusCode().value());
        assertEquals(idCampaign, response.getBody());
    }

    @Test
    void testMainGenesisLunaticOnly_KraftwerkException() throws Exception {
        String idCampaign = "test-campaign-id";
        KraftwerkException exception = new KraftwerkException( HttpStatus.BAD_REQUEST.value(), "Kraftwerk error");


        // Mock the dependencies
        FileUtilsInterface mockFileUtilsInterface = mock(FileUtilsInterface.class);
        MainProcessingGenesis mockMainProcessing = mock(MainProcessingGenesis.class);
        MainService mainService  = Mockito.spy(new MainService(configProperties,minioConfig));
        doReturn(mockFileUtilsInterface).when(mainService).getFileUtilsInterface();
        doReturn(mockMainProcessing).when(mainService).getMainProcessingGenesis(anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doThrow(exception).when(mockMainProcessing).runMain(idCampaign,100);

        // WHEN
        ResponseEntity<String> response = mainService.mainGenesisLunaticOnly(idCampaign,100, false);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Kraftwerk error", response.getBody());
    }

    @Test
    void testMainGenesisLunaticOnly_IOException() throws Exception {
        String idCampaign = "test-campaign-id";
        IOException exception = new IOException("IO error");

        // Mock the dependencies
        FileUtilsInterface mockFileUtilsInterface = mock(FileUtilsInterface.class);
        MainProcessingGenesis mockMainProcessing = mock(MainProcessingGenesis.class);
        MainService mainService  = Mockito.spy(new MainService(configProperties,minioConfig));
        doReturn(mockFileUtilsInterface).when(mainService).getFileUtilsInterface();
        doReturn(mockMainProcessing).when(mainService).getMainProcessingGenesis(anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doThrow(exception).when(mockMainProcessing).runMain(idCampaign,100);

        // WHEN
        ResponseEntity<String> response = mainService.mainGenesisLunaticOnly(idCampaign,100, false);

        // THEN
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("IO error", response.getBody());
    }
}