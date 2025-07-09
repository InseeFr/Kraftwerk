package fr.insee.kraftwerk.api.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;


class ArgsCheckerTest {

    @MockitoBean
    private ArgsChecker argsChecker;

    @Test
    void testBadNbOfArgs_1() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("Invalid number of arguments", exception.getMessage());
    }

    @Test
    void testBadNbOfArgs_2() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("SERVICE_NAME");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("Invalid number of arguments", exception.getMessage());
    }

    @Test
    void testBadServiceName() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("SERVICE_NAME");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("Invalid argServiceName argument ! : SERVICE_NAME", exception.getMessage());
    }

    @Test
    void testGoodServiceName_MAIN() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(KraftwerkServiceType.MAIN, argsChecker.getServiceName());
        assertEquals(Boolean.TRUE, argsChecker.isWithDDI());
    }

    @Test
    void testGoodServiceName_LUNATIC_ONLY() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("LUNATIC_ONLY");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(KraftwerkServiceType.LUNATIC_ONLY, argsChecker.getServiceName());
        assertEquals(Boolean.FALSE, argsChecker.isWithDDI());
    }


    @Test
    void testGoodServiceName_GENESIS() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("GENESIS");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(KraftwerkServiceType.GENESIS, argsChecker.getServiceName());
        assertEquals(Boolean.TRUE, argsChecker.isWithDDI());
    }


    @Test
    void testGoodServiceName_GENESISV2() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("GENESISV2");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(KraftwerkServiceType.GENESISV2, argsChecker.getServiceName());
        assertEquals(Boolean.TRUE, argsChecker.isWithDDI());
    }


    @Test
    void testGoodServiceName_FILE_BY_FILE() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("FILE_BY_FILE");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(KraftwerkServiceType.FILE_BY_FILE, argsChecker.getServiceName());
        assertEquals(Boolean.TRUE, argsChecker.isFileByFile());
        assertEquals(Boolean.TRUE, argsChecker.isWithDDI());
    }


    @Test
    void testIsArchive_NULL() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsArchive()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isArchive());
    }


    @Test
    void testIsArchive_FALSE() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsArchive("false");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsArchive()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isArchive());
    }


    @Test
    void testIsArchive_TRUE() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsArchive("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsArchive()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.TRUE, argsChecker.isArchive());
    }


    @Test
    void testIsArchive_FORCE_1() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("GENESIS");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsArchive("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        doNothing().when(argsChecker).checkArgNumber();
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsArchive()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isArchive());
    }


    @Test
    void testIsArchive_FORCE_2() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("GENESISV2");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsArchive("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        doNothing().when(argsChecker).checkArgNumber();
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsArchive()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isArchive());
    }


    @Test
    void testIsArchive_BAD_VALUE() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsArchive("bad_boolean");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsArchive()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("Invalid argIsArchive boolean argument ! : bad_boolean", exception.getMessage());
    }


    @Test
    void testIsReportingData_NULL() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsArchive()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsReportingData()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isReportingData());
    }


    @Test
    void testIsReportingData_FALSE() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsReportingData("false");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsArchive()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsReportingData()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isReportingData());
    }


    @Test
    void testIsReportingData_TRUE() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsReportingData("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsArchive()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsReportingData()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.TRUE, argsChecker.isReportingData());
    }


    private static Stream<Arguments> isReportingDataParameterizedTests() {
        return Stream.of(
                Arguments.of(Boolean.TRUE, "MAIN"),
                Arguments.of(Boolean.FALSE, "LUNATIC_ONLY"),
                Arguments.of(Boolean.FALSE, "GENESIS"),
                Arguments.of(Boolean.FALSE, "GENESISV2"),
                Arguments.of(Boolean.FALSE, "FILE_BY_FILE")
        );
    }


    @ParameterizedTest
    @MethodSource("isReportingDataParameterizedTests")
    void testIsReportingData_ParameterizedTests(boolean expectedResult, String param) {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName(param); // This will force "IsReportingData" to "FALSE" because != "MAIN"!
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsReportingData("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        doNothing().when(argsChecker).checkArgNumber();
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgIsArchive();
        //NOTE : "argsChecker.checkArgIsReportingData()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(expectedResult, argsChecker.isReportingData());
    }


    @Test
    void testIsReportingData_BAD_VALUE() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsReportingData("bad_boolean");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsArchive()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsReportingData()" is not ignored on this unit test
        doNothing().when(argsChecker).checkArgWithEncryption();

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("Invalid argIsReportingData boolean argument ! : bad_boolean", exception.getMessage());
    }


    @Test
    void testWithEncryption_NULL() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsArchive()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsReportingData()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgWithEncryption()" is not ignored on this unit test

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isWithEncryption());
    }


    @Test
    void testWithEncryption_FALSE() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWithEncryption("false");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsArchive()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsReportingData()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgWithEncryption()" is not ignored on this unit test

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isWithEncryption());
    }


    @Test
    void testWithEncryption_TRUE() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWithEncryption("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsArchive()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsReportingData()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgWithEncryption()" is not ignored on this unit test

        argsChecker.checkArgs();

        assertEquals(Boolean.TRUE, argsChecker.isWithEncryption());
    }


    @Test
    void testWithEncryption_BAD_VALUE() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWithEncryption("bad_boolean");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //NOTE : "argsChecker.checkArgNumber()" is not ignored on this unit test
        //NOTE : "argsChecker.checkServiceName()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsArchive()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgIsReportingData()" is not ignored on this unit test
        //NOTE : "argsChecker.checkArgWithEncryption()" is not ignored on this unit test

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("Invalid argWithEncryption boolean argument ! : bad_boolean", exception.getMessage());
    }


    @Test
    void testWorkerNb_NOT_INTEGER() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkersNb("bad_integer");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("arg (argWorkersNb) cannot be parsed as an integer !", exception.getMessage());
    }


    @Test
    void testWorkerIndex_NOT_INTEGER() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkerIndex("bad_integer");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("arg (argWorkerIndex) cannot be parsed as an integer !", exception.getMessage());
    }


    @Test
    void testWorkerNb_DEFAULT() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        argsChecker.checkArgs();

        assertEquals(1, argsChecker.getWorkersNb());
    }


    @Test
    void testWorkerIndex_DEFAULT() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        argsChecker.checkArgs();

        assertEquals(1, argsChecker.getWorkerIndex());
    }

    @Test
    void testWorkerNb_OK() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkersNb("3");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        argsChecker.checkArgs();

        assertEquals(3, argsChecker.getWorkersNb());
    }


    @Test
    void testWorkerIndex_OK() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkerIndex("1");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        argsChecker.checkArgs();

        assertEquals(1, argsChecker.getWorkerIndex());
    }


    @Test
    void testWorkerNb_BAD_INTEGER_1() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkersNb("0");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("workers must be between 1 and 10 ! (got 0)", exception.getMessage());
    }


    @Test
    void testWorkerNb_BAD_INTEGER_2() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkersNb("11");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("workers must be between 1 and 10 ! (got 11)", exception.getMessage());
    }


    @Test
    void testWorkerIndex_BAD_INTEGER_1() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkerIndex("0");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("workerId cannot be > workers number, which is inconsistant ! (got 0 for 1 workers)", exception.getMessage());
    }


    @Test
    void testWorkerIndex_BAD_INTEGER_2() {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkersNb("3");
        argsCheckerBuilder.argWorkerIndex("4");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("workerId cannot be > workers number, which is inconsistant ! (got 4 for 3 workers)", exception.getMessage());
    }



}
