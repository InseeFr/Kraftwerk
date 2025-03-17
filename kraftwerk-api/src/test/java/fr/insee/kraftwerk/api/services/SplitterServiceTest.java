package fr.insee.kraftwerk.api.services;

import fr.insee.kraftwerk.TestConfig;
import fr.insee.kraftwerk.TestUtils;
import fr.insee.kraftwerk.core.utils.xml.XmlSplitter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestConfig.class)
class SplitterServiceTest {

    @Autowired
    private MockMvc mockMvc; // Simulates HTTP requests to the REST endpoints

    // Constants for user roles
    private static final String USER_KRAFTWERK = "USER";
    private static final String ADMIN = "ADMIN";

    // API endpoints under test
    private static final String URI_SPLITTER = "/split/lunatic-xml";

    // Mock beans to replace actual service implementations
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private SplitterService splitterService;
    @Autowired
    private TestUtils testUtils;


    /**
     * Test that users with the "USER" role can't access the splitter endpoint.
     */
    @Test
    @DisplayName("Kraftwerk users should not access splitter service")
    void kraftwerk_users_should_not_access_steps_services() throws Exception{
        try(MockedStatic<XmlSplitter> mockedStatic = mockStatic(XmlSplitter.class)) {
            mockedStatic.when(() -> XmlSplitter.split(anyString(),anyString(),anyString(),anyString(),anyInt(), any())).thenAnswer(invocation -> null);
            Jwt jwt = testUtils.generateJwt(List.of("USER"), USER_KRAFTWERK);
            when(jwtDecoder.decode(anyString())).thenReturn(jwt);
            mockMvc.perform(put(URI_SPLITTER).header("Authorization", "bearer token_blabla")
                            .param("inputFolder", "input_folder")
                            .param("outputFolder", "output_folder")
                            .param("filename", "test_file")
                            .param("nbResponsesByFile", "2")
                            .param("fileSystemType", "OS_FILESYSTEM"))
                    .andExpect(status().isForbidden());
        }
    }

    /**
     * Test that admins can access the splitter endpoint.
     */
    @Test
    @DisplayName("Admins should access splitter service")
    void admins_should_access_splitter_service() throws Exception{
        try(MockedStatic<XmlSplitter> mockedStatic = mockStatic(XmlSplitter.class)) {
            mockedStatic.when(() -> XmlSplitter.split(anyString(),anyString(),anyString(),anyString(),anyInt(), any())).thenAnswer(invocation -> null);
            Jwt jwt = testUtils.generateJwt(List.of("ADMIN"), ADMIN);
            when(jwtDecoder.decode(anyString())).thenReturn(jwt);
            mockMvc.perform(put(URI_SPLITTER).header("Authorization", "bearer token_blabla")
                            .param("inputFolder", "input_folder")
                            .param("outputFolder", "output_folder")
                            .param("filename", "test_file")
                            .param("nbResponsesByFile", "2")
                            .param("fileSystemType", "OS_FILESYSTEM"))
                    .andExpect(status().isOk());
        }
    }

    /**
     * Test that users with incorrect roles can't access the splitter endpoint.
     */
    @Test
    @DisplayName("Incorrect roles should not access splitter service")
    void users_with_incorrect_roles_should_not_access_steps_services() throws Exception{
        try(MockedStatic<XmlSplitter> mockedStatic = mockStatic(XmlSplitter.class)) {
            mockedStatic.when(() -> XmlSplitter.split(anyString(),anyString(),anyString(),anyString(),anyInt(), any())).thenAnswer(invocation -> null);
            Jwt jwt = testUtils.generateJwt(List.of(""), "incorrect_role");
            when(jwtDecoder.decode(anyString())).thenReturn(jwt);
            mockMvc.perform(put(URI_SPLITTER).header("Authorization", "bearer token_blabla")
                            .param("inputFolder", "input_folder")
                            .param("outputFolder", "output_folder")
                            .param("filename", "test_file")
                            .param("nbResponsesByFile", "2")
                            .param("fileSystemType", "OS_FILESYSTEM"))
                    .andExpect(status().isForbidden());
        }
    }


}
