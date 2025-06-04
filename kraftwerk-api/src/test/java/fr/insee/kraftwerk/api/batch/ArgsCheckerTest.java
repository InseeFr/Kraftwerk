package fr.insee.kraftwerk.api.batch;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;


class ArgsCheckerTest {

    @MockitoBean
    private ArgsChecker argsChecker;

    @Test
    void testBadNbOfArgs_1() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("Invalid number of arguments", exception.getMessage());
    }

    @Test
    void testBadNbOfArgs_2() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("SERVICE_NAME");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("Invalid number of arguments", exception.getMessage());
    }

    @Test
    void testBadServiceName() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("SERVICE_NAME");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("Invalid argServiceName argument ! : SERVICE_NAME", exception.getMessage());
    }

    @Test
    void testGoodServiceName_MAIN() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(KraftwerkServiceType.MAIN, argsChecker.getServiceName());
        assertEquals(Boolean.TRUE, argsChecker.isWithDDI());
    }

    @Test
    void testGoodServiceName_LUNATIC_ONLY() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("LUNATIC_ONLY");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(KraftwerkServiceType.LUNATIC_ONLY, argsChecker.getServiceName());
        assertEquals(Boolean.FALSE, argsChecker.isWithDDI());
    }


    @Test
    void testGoodServiceName_GENESIS() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("GENESIS");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(KraftwerkServiceType.GENESIS, argsChecker.getServiceName());
        assertEquals(Boolean.TRUE, argsChecker.isWithDDI());
    }


    @Test
    void testGoodServiceName_GENESISV2() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("GENESISV2");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(KraftwerkServiceType.GENESISV2, argsChecker.getServiceName());
        assertEquals(Boolean.TRUE, argsChecker.isWithDDI());
    }


    @Test
    void testGoodServiceName_FILE_BY_FILE() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("FILE_BY_FILE");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(KraftwerkServiceType.FILE_BY_FILE, argsChecker.getServiceName());
        assertEquals(Boolean.TRUE, argsChecker.isFileByFile());
        assertEquals(Boolean.TRUE, argsChecker.isWithDDI());
    }


    @Test
    void testIsArchive_NULL() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        //doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isArchive());
    }


    @Test
    void testIsArchive_FALSE() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsArchive("false");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        //doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isArchive());
    }


    @Test
    void testIsArchive_TRUE() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsArchive("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        //doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.TRUE, argsChecker.isArchive());
    }


    @Test
    void testIsArchive_FORCE_1() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("GENESIS");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsArchive("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        //doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isArchive());
    }


    @Test
    void testIsArchive_FORCE_2() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("GENESISV2");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsArchive("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        //doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isArchive());
    }


    @Test
    void testIsArchive_BAD_VALUE() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsArchive("bad_boolean");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        //doNothing().when(argsChecker).checkArgIsArchive();
        doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("Invalid argIsArchive boolean argument ! : bad_boolean", exception.getMessage());
    }


    @Test
    void testIsReportingData_NULL() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        //doNothing().when(argsChecker).checkArgIsArchive();
        //doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isReportingData());
    }


    @Test
    void testIsReportingData_FALSE() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsReportingData("false");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        //doNothing().when(argsChecker).checkArgIsArchive();
        //doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isReportingData());
    }


    @Test
    void testIsReportingData_TRUE() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsReportingData("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        //doNothing().when(argsChecker).checkArgIsArchive();
        //doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.TRUE, argsChecker.isReportingData());
    }


    @Test
    void testIsReportingData_MAIN() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN"); // This will set "IsReportingData" to arg content because == "MAIN"!
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsReportingData("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        //doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.TRUE, argsChecker.isReportingData());
    }


    @Test
    void testIsReportingData_LUNATIC_ONLY() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("LUNATIC_ONLY"); // This will force "IsReportingData" to "FALSE" because != "MAIN"!
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsReportingData("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        //doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isReportingData());
    }


    @Test
    void testIsReportingData_GENESIS() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("GENESIS"); // This will force "IsReportingData" to "FALSE" because != "MAIN"!
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsReportingData("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        //doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isReportingData());
    }


    @Test
    void testIsReportingData_GENESISV2() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("GENESISV2"); // This will force "IsReportingData" to "FALSE" because != "MAIN"!
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsReportingData("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        //doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isReportingData());
    }


    @Test
    void testIsReportingData_FILE_BY_FILE() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("FILE_BY_FILE"); // This will force "IsReportingData" to "FALSE" because != "MAIN"!
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsReportingData("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        doNothing().when(argsChecker).checkArgIsArchive();
        //doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isReportingData());
    }


    @Test
    void testIsReportingData_BAD_VALUE() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argIsReportingData("bad_boolean");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        //doNothing().when(argsChecker).checkArgIsArchive();
        //doNothing().when(argsChecker).checkArgIsReportingData();
        doNothing().when(argsChecker).checkArgWithEncryption();

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("Invalid argIsReportingData boolean argument ! : bad_boolean", exception.getMessage());
    }


    @Test
    void testWithEncryption_NULL() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        //doNothing().when(argsChecker).checkArgIsArchive();
        //doNothing().when(argsChecker).checkArgIsReportingData();
        //doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isWithEncryption());
    }


    @Test
    void testWithEncryption_FALSE() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWithEncryption("false");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        //doNothing().when(argsChecker).checkArgIsArchive();
        //doNothing().when(argsChecker).checkArgIsReportingData();
        //doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.FALSE, argsChecker.isWithEncryption());
    }


    @Test
    void testWithEncryption_TRUE() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWithEncryption("true");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        //doNothing().when(argsChecker).checkArgIsArchive();
        //doNothing().when(argsChecker).checkArgIsReportingData();
        //doNothing().when(argsChecker).checkArgWithEncryption();

        argsChecker.checkArgs();

        assertEquals(Boolean.TRUE, argsChecker.isWithEncryption());
    }


    @Test
    void testWithEncryption_BAD_VALUE() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWithEncryption("bad_boolean");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());
        //doNothing().when(argsChecker).checkArgNumber();
        //doNothing().when(argsChecker).checkServiceName();
        //doNothing().when(argsChecker).checkArgIsArchive();
        //doNothing().when(argsChecker).checkArgIsReportingData();
        //doNothing().when(argsChecker).checkArgWithEncryption();

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("Invalid argWithEncryption boolean argument ! : bad_boolean", exception.getMessage());
    }


    @Test
    void testWorkerNb_NOT_INTEGER() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkersNb("bad_integer");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("arg (argWorkersNb) cannot be parsed as an integer !", exception.getMessage());
    }


    @Test
    void testWorkerIndex_NOT_INTEGER() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkerIndex("bad_integer");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("arg (argWorkerIndex) cannot be parsed as an integer !", exception.getMessage());
    }


    @Test
    void testWorkerNb_DEFAULT() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        argsChecker.checkArgs();

        assertEquals(1, argsChecker.getWorkersNb());
    }


    @Test
    void testWorkerIndex_DEFAULT() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        argsChecker.checkArgs();

        assertEquals(1, argsChecker.getWorkerIndex());
    }

    @Test
    void testWorkerNb_OK() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkersNb("3");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        argsChecker.checkArgs();

        assertEquals(3, argsChecker.getWorkersNb());
    }


    @Test
    void testWorkerIndex_OK() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkerIndex("1");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        argsChecker.checkArgs();

        assertEquals(1, argsChecker.getWorkerIndex());
    }


    @Test
    void testWorkerNb_BAD_INTEGER_1() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkersNb("0");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("workers must be between 1 and 10 ! (got 0)", exception.getMessage());
    }


    @Test
    void testWorkerNb_BAD_INTEGER_2() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkersNb("11");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("workers must be between 1 and 10 ! (got 11)", exception.getMessage());
    }


    @Test
    void testWorkerIndex_BAD_INTEGER_1() throws Exception {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        argsCheckerBuilder.argServiceName("MAIN");
        argsCheckerBuilder.argCampaignId("CAMPAIGN_ID");
        argsCheckerBuilder.argWorkerIndex("0");
        argsChecker  = Mockito.spy(argsCheckerBuilder.build());

        Throwable exception = assertThrows(IllegalArgumentException.class, argsChecker::checkArgs);
        assertEquals("workerId cannot be > workers number, which is inconsistant ! (got 0 for 1 workers)", exception.getMessage());
    }


    @Test
    void testWorkerIndex_BAD_INTEGER_2() throws Exception {
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
