package fr.insee.kraftwerk.api.batch;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.api.process.MainProcessingGenesis;
import fr.insee.kraftwerk.api.services.KraftwerkService;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KraftwerkBatchTest {

    @Mock
    ConfigProperties configProperties;
    @Mock
    MinioConfig minioConfig;

    @Spy
    @InjectMocks
    KraftwerkBatch kraftwerkBatch;

    @Test
    void noArgs_Test() throws Exception {
        DefaultApplicationArguments myArgs = new DefaultApplicationArguments(new String[]{});
        kraftwerkBatch.run(myArgs);
        verify(kraftwerkBatch, times(0)).cliModeLaunched(any());
    }


    @Test
    void throwException_Test() throws Exception {
        //Use Case
        DefaultApplicationArguments myArgs = new DefaultApplicationArguments(new String[]{"--aaa=bbb"});
        //Mocks : unnecessary here as an exception will be thrown before all useful calls : invalid arguments
        //execute
        kraftwerkBatch.run(myArgs);
        //Checks
        verify(kraftwerkBatch, times(1)).cliModeLaunched(any());
    }


    @Test
    void run_allArgs_Test() throws Exception {
        //Use Case
        String args = "--service=MAIN;--campaignId=TEST-GLOB;--archive=true;--reporting-data=false;--with-encryption=false;--workers-nb=1;--worker-index=1";
        String[] argsArray = args.split(";");
        DefaultApplicationArguments myArgs = new DefaultApplicationArguments(argsArray);

        //Mocks : N/A here

        MainProcessing mockMainProcessing = mock(MainProcessing.class);
        doReturn(mockMainProcessing).when(kraftwerkBatch).getMainProcessing(any(KraftwerkExecutionContext.class));
        doNothing().when(mockMainProcessing).runMain();

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<?> responseEntity = new ResponseEntity<>(
                "some response body",
                header,
                HttpStatus.OK
        );
        KraftwerkService mockKraftwerkService = mock(KraftwerkService.class);
        doReturn(mockKraftwerkService).when(kraftwerkBatch).getKraftwerkService();
        doReturn(responseEntity).when(mockKraftwerkService).archive(any(), any());

        //execute
        kraftwerkBatch.run(myArgs);

        //Checks
        verify(kraftwerkBatch, times(1)).cliModeLaunched(any());
        verify(mockMainProcessing, times(1)).runMain();
        verify(mockKraftwerkService, times(1)).archive(any(), any());
    }


    private static Stream<Arguments> mainProcessingParameterizedTests() {
        return Stream.of(
                Arguments.of("MAIN"),
                Arguments.of("LUNATIC_ONLY"),
                Arguments.of("FILE_BY_FILE")
        );
    }

    @ParameterizedTest
    @MethodSource("mainProcessingParameterizedTests")
    void run_mainProcessing_Test(String serviceName) throws Exception {
        //Use Case
        String args = "--service=" + serviceName + ";--campaignId=TEST-GLOB";
        String[] argsArray = args.split(";");
        DefaultApplicationArguments myArgs = new DefaultApplicationArguments(argsArray);

        //Mocks : N/A here

        MainProcessing mockMainProcessing = mock(MainProcessing.class);
        doReturn(mockMainProcessing).when(kraftwerkBatch).getMainProcessing(any(KraftwerkExecutionContext.class));
        doNothing().when(mockMainProcessing).runMain();

        //execute
        kraftwerkBatch.run(myArgs);

        //Checks
        verify(kraftwerkBatch, times(1)).cliModeLaunched(any());
        verify(mockMainProcessing, times(1)).runMain();
    }




    @Test
    void run_GENESIS_Test() throws Exception {
        //Use Case
        String args = "--service=GENESIS;--campaignId=TEST-GLOB";
        String[] argsArray = args.split(";");
        DefaultApplicationArguments myArgs = new DefaultApplicationArguments(argsArray);

        //Mocks
        MainProcessingGenesis mockMainProcessingGenesis = mock(MainProcessingGenesis.class);
        doReturn(mockMainProcessingGenesis).when(kraftwerkBatch).getMainProcessingGenesis(any(KraftwerkExecutionContext.class));
        doNothing().when(mockMainProcessingGenesis).runMain(any(), anyInt(), anyInt(), anyInt());

        //execute
        kraftwerkBatch.run(myArgs);

        //Checks
        verify(kraftwerkBatch, times(1)).cliModeLaunched(any());
        verify(mockMainProcessingGenesis, times(1)).runMain(any(), anyInt(), anyInt(), anyInt());
    }

}
