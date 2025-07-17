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
class KraftwerkBatchV2Test {

    @Mock
    ConfigProperties configProperties;
    @Mock
    MinioConfig minioConfig;

    @Spy
    @InjectMocks
    KraftwerkBatchV2 kraftwerkBatchV2;

    @Test
    public void noArgs_Test() throws Exception {
        DefaultApplicationArguments myArgs = new DefaultApplicationArguments(new String[]{});
        kraftwerkBatchV2.run(myArgs);
        verify(kraftwerkBatchV2, times(0)).cliModeLaunched(any());
    }


    @Test
    public void throwException_Test() throws Exception {
        //Use Case
        DefaultApplicationArguments myArgs = new DefaultApplicationArguments(new String[]{"--aaa=bbb"});
        //Mocks : unnecessary here as an exception will be thrown before all useful calls : invalid arguments
        //execute
        kraftwerkBatchV2.run(myArgs);
        //Checks
        verify(kraftwerkBatchV2, times(1)).cliModeLaunched(any());
    }


    @Test
    public void run_allArgs_Test() throws Exception {
        //Use Case
        String args = "--service=MAIN;--campaignId=TEST-GLOB;--archive=true;--reporting-data=false;--with-encryption=false;--workers-nb=1;--worker-index=1";
        String[] argsArray = args.split(";");
        DefaultApplicationArguments myArgs = new DefaultApplicationArguments(argsArray);

        //Mocks
        //NOT NEEDED MOCK FOR THAT TEST-CASE : MainProcessingGenesis mockMainProcessingGenesis = mock(MainProcessingGenesis.class);
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doReturn(mockMainProcessingGenesis).when(kraftwerkBatchV2).getMainProcessingGenesis(any(KraftwerkExecutionContext.class));
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doNothing().when(mockMainProcessingGenesis).runMain(any(),anyInt());
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doNothing().when(mockMainProcessingGenesis).runMainV2(any(), anyInt(), anyInt(), anyInt());

        MainProcessing mockMainProcessing = mock(MainProcessing.class);
        doReturn(mockMainProcessing).when(kraftwerkBatchV2).getMainProcessing(any(KraftwerkExecutionContext.class));
        doNothing().when(mockMainProcessing).runMain();

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<?> responseEntity = new ResponseEntity<>(
                "some response body",
                header,
                HttpStatus.OK
        );
        KraftwerkService mockKraftwerkService = mock(KraftwerkService.class);
        doReturn(mockKraftwerkService).when(kraftwerkBatchV2).getKraftwerkService();
        doReturn(responseEntity).when(mockKraftwerkService).archive(any(), any());

        //execute
        kraftwerkBatchV2.run(myArgs);

        //Checks
        verify(kraftwerkBatchV2, times(1)).cliModeLaunched(any());
        verify(mockMainProcessing, times(1)).runMain();
        //NOT NEEDED VERIFICATION FOR THAT TEST-CASE : verify(mockMainProcessingGenesis, times(0)).runMain(any(), anyInt());
        //NOT NEEDED VERIFICATION FOR THAT TEST-CASE : verify(mockMainProcessingGenesis, times(0)).runMainV2(any(), anyInt(), anyInt(), anyInt());
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
    public void run_mainProcessing_Test(String serviceName) throws Exception {
        //Use Case
        String args = "--service=" + serviceName + ";--campaignId=TEST-GLOB";
        String[] argsArray = args.split(";");
        DefaultApplicationArguments myArgs = new DefaultApplicationArguments(argsArray);

        //Mocks
        //NOT NEEDED MOCK FOR THAT TEST-CASE : MainProcessingGenesis mockMainProcessingGenesis = mock(MainProcessingGenesis.class);
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doReturn(mockMainProcessingGenesis).when(kraftwerkBatchV2).getMainProcessingGenesis(any(KraftwerkExecutionContext.class));
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doNothing().when(mockMainProcessingGenesis).runMain(any(),anyInt());
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doNothing().when(mockMainProcessingGenesis).runMainV2(any(), anyInt(), anyInt(), anyInt());

        MainProcessing mockMainProcessing = mock(MainProcessing.class);
        doReturn(mockMainProcessing).when(kraftwerkBatchV2).getMainProcessing(any(KraftwerkExecutionContext.class));
        doNothing().when(mockMainProcessing).runMain();

        //NOT NEEDED MOCK FOR THAT TEST-CASE : KraftwerkService mockKraftwerkService = mock(KraftwerkService.class);
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doReturn(mockKraftwerkService).when(kraftwerkBatchV2).getKraftwerkService();
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doReturn(null).when(mockKraftwerkService).archive(any(), any());

        //execute
        kraftwerkBatchV2.run(myArgs);

        //Checks
        verify(kraftwerkBatchV2, times(1)).cliModeLaunched(any());
        verify(mockMainProcessing, times(1)).runMain();
        //NOT NEEDED VERIFICATION FOR THAT TEST-CASE : verify(mockMainProcessingGenesis, times(0)).runMain(any(), anyInt());
        //NOT NEEDED VERIFICATION FOR THAT TEST-CASE : verify(mockMainProcessingGenesis, times(0)).runMainV2(any(), anyInt(), anyInt(), anyInt());
        //NOT NEEDED VERIFICATION FOR THAT TEST-CASE : verify(mockKraftwerkService, times(0)).archive(any(), any());
    }


    @Test
    public void run_GENESIS_Test() throws Exception {
        //Use Case
        String args = "--service=GENESIS;--campaignId=TEST-GLOB";
        String[] argsArray = args.split(";");
        DefaultApplicationArguments myArgs = new DefaultApplicationArguments(argsArray);

        //Mocks
        MainProcessingGenesis mockMainProcessingGenesis = mock(MainProcessingGenesis.class);
        doReturn(mockMainProcessingGenesis).when(kraftwerkBatchV2).getMainProcessingGenesis(any(KraftwerkExecutionContext.class));
        doNothing().when(mockMainProcessingGenesis).runMain(any(),anyInt());
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doNothing().when(mockMainProcessingGenesis).runMainV2(any(), anyInt(), anyInt(), anyInt());

        //NOT NEEDED MOCK FOR THAT TEST-CASE : MainProcessing mockMainProcessing = mock(MainProcessing.class);
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doReturn(mockMainProcessing).when(kraftwerkBatchV2).getMainProcessing(any(KraftwerkExecutionContext.class));
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doNothing().when(mockMainProcessing).runMain();

        //NOT NEEDED MOCK FOR THAT TEST-CASE : KraftwerkService mockKraftwerkService = mock(KraftwerkService.class);
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doReturn(mockKraftwerkService).when(kraftwerkBatchV2).getKraftwerkService();
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doReturn(null).when(mockKraftwerkService).archive(any(), any());

        //execute
        kraftwerkBatchV2.run(myArgs);

        //Checks
        verify(kraftwerkBatchV2, times(1)).cliModeLaunched(any());
        //NOT NEEDED VERIFICATION FOR THAT TEST-CASE : verify(mockMainProcessing, times(0)).runMain();
        verify(mockMainProcessingGenesis, times(1)).runMain(any(), anyInt());
        verify(mockMainProcessingGenesis, times(0)).runMainV2(any(), anyInt(), anyInt(), anyInt());
        //NOT NEEDED VERIFICATION FOR THAT TEST-CASE : verify(mockKraftwerkService, times(0)).archive(any(), any());
    }


    @Test
    public void run_GENESISV2_Test() throws Exception {
        //Use Case
        String args = "--service=GENESISV2;--campaignId=TEST-GLOB";
        String[] argsArray = args.split(";");
        DefaultApplicationArguments myArgs = new DefaultApplicationArguments(argsArray);

        //Mocks
        MainProcessingGenesis mockMainProcessingGenesis = mock(MainProcessingGenesis.class);
        doReturn(mockMainProcessingGenesis).when(kraftwerkBatchV2).getMainProcessingGenesis(any(KraftwerkExecutionContext.class));
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doNothing().when(mockMainProcessingGenesis).runMain(any(),anyInt());
        doNothing().when(mockMainProcessingGenesis).runMainV2(any(), anyInt(), anyInt(), anyInt());

        //NOT NEEDED MOCK FOR THAT TEST-CASE : MainProcessing mockMainProcessing = mock(MainProcessing.class);
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doReturn(mockMainProcessing).when(kraftwerkBatchV2).getMainProcessing(any(KraftwerkExecutionContext.class));
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doNothing().when(mockMainProcessing).runMain();

        //NOT NEEDED MOCK FOR THAT TEST-CASE : KraftwerkService mockKraftwerkService = mock(KraftwerkService.class);
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doReturn(mockKraftwerkService).when(kraftwerkBatchV2).getKraftwerkService();
        //NOT NEEDED MOCK FOR THAT TEST-CASE : doReturn(null).when(mockKraftwerkService).archive(any(), any());

        //execute
        kraftwerkBatchV2.run(myArgs);

        //Checks
        verify(kraftwerkBatchV2, times(1)).cliModeLaunched(any());
        //NOT NEEDED VERIFICATION FOR THAT TEST-CASE : verify(mockMainProcessing, times(0)).runMain();
        verify(mockMainProcessingGenesis, times(0)).runMain(any(), anyInt());
        verify(mockMainProcessingGenesis, times(1)).runMainV2(any(), anyInt(), anyInt(), anyInt());
        //NOT NEEDED VERIFICATION FOR THAT TEST-CASE : verify(mockKraftwerkService, times(0)).archive(any(), any());
    }

}
