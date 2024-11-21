package fr.insee.kraftwerk.api.services;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.api.process.MainProcessingGenesis;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ActiveProfiles("test")
@SpringBootTest
class MainServiceTest {

    ConfigProperties configProperties = new ConfigProperties();
    MinioConfig minioConfig;

    /*** Main ***/

    @Test
    void testMain_Success() throws Exception {
        String inDirectory = "test-campaign-id";

        // Mock the dependencies
        FileUtilsInterface mockFileUtilsInterface = mock(FileUtilsInterface.class);
        MainProcessing mockMainProcessing = mock(MainProcessing.class);
        MainService mainService  = Mockito.spy(new MainService(configProperties,minioConfig));
        doReturn(mockFileUtilsInterface).when(mainService).getFileUtilsInterface();
        doReturn(mockMainProcessing).when(mainService).getMainProcessing(anyString(),anyBoolean(),anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doNothing().when(mockMainProcessing).runMain();

        // WHEN
        ResponseEntity<String> response = mainService.mainService(inDirectory, false, false);

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
        doReturn(mockMainProcessing).when(mainService).getMainProcessing(anyString(),anyBoolean(),anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doThrow(exception).when(mockMainProcessing).runMain();

        // WHEN
        ResponseEntity<String> response = mainService.mainService(inDirectory, false, false);

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
        doReturn(mockMainProcessing).when(mainService).getMainProcessing(anyString(),anyBoolean(),anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doNothing().when(mockMainProcessing).runMain();

        // WHEN
        ResponseEntity<String> response = mainService.mainFileByFile(inDirectory, false);

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
        doReturn(mockMainProcessing).when(mainService).getMainProcessing(anyString(),anyBoolean(),anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doThrow(exception).when(mockMainProcessing).runMain();

        // WHEN
        ResponseEntity<String> response = mainService.mainFileByFile(inDirectory, false);

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
        doReturn(mockMainProcessing).when(mainService).getMainProcessing(anyString(),anyBoolean(),anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doNothing().when(mockMainProcessing).runMain();

        // WHEN
        ResponseEntity<String> response = mainService.mainLunaticOnly(inDirectory, false);

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
        doReturn(mockMainProcessing).when(mainService).getMainProcessing(anyString(),anyBoolean(),anyBoolean(),anyBoolean(),any(FileUtilsInterface.class));
        doThrow(exception).when(mockMainProcessing).runMain();

        // WHEN
        ResponseEntity<String> response = mainService.mainLunaticOnly(inDirectory, false);

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
        doReturn(mockMainProcessing).when(mainService).getMainProcessingGenesis(anyBoolean(),any(FileUtilsInterface.class));
        doNothing().when(mockMainProcessing).runMain(idCampaign);

        // WHEN
        ResponseEntity<String> response = mainService.mainGenesis(idCampaign);

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
        doReturn(mockMainProcessing).when(mainService).getMainProcessingGenesis(anyBoolean(),any(FileUtilsInterface.class));
        doThrow(exception).when(mockMainProcessing).runMain(idCampaign);

        // WHEN
        ResponseEntity<String> response = mainService.mainGenesis(idCampaign);

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
        doReturn(mockMainProcessing).when(mainService).getMainProcessingGenesis(anyBoolean(),any(FileUtilsInterface.class));
        doThrow(exception).when(mockMainProcessing).runMain(idCampaign);

        // WHEN
        ResponseEntity<String> response = mainService.mainGenesis(idCampaign);

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
        doReturn(mockMainProcessing).when(mainService).getMainProcessingGenesis(anyBoolean(),any(FileUtilsInterface.class));
        doNothing().when(mockMainProcessing).runMain(idCampaign);

        // WHEN
        ResponseEntity<String> response = mainService.mainGenesisLunaticOnly(idCampaign);

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
        doReturn(mockMainProcessing).when(mainService).getMainProcessingGenesis(anyBoolean(),any(FileUtilsInterface.class));
        doThrow(exception).when(mockMainProcessing).runMain(idCampaign);

        // WHEN
        ResponseEntity<String> response = mainService.mainGenesisLunaticOnly(idCampaign);

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
        doReturn(mockMainProcessing).when(mainService).getMainProcessingGenesis(anyBoolean(),any(FileUtilsInterface.class));
        doThrow(exception).when(mockMainProcessing).runMain(idCampaign);

        // WHEN
        ResponseEntity<String> response = mainService.mainGenesisLunaticOnly(idCampaign);

        // THEN
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("IO error", response.getBody());
    }

}
