package fr.insee.kraftwerk.api.process;

import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.extradata.reportingdata.CSVReportingDataParser;
import fr.insee.kraftwerk.core.extradata.reportingdata.XMLReportingDataParser;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportingDataProcessingTest {

    @MockitoBean
    private ReportingDataProcessing spyReportingDataProcessing;

    @Test
    void runProcessMain_Test() throws KraftwerkException {
        // 1. Mock the dependencies
        spyReportingDataProcessing = Mockito.spy(new ReportingDataProcessing());
        doNothing().when(spyReportingDataProcessing).runProcess(any(), any(), any(), any());

        String defaultDir = "/a/b/c/defaultDir";
        String inDirectory = "inDir";
        Path inDirParam = Path.of(defaultDir, "in", inDirectory);

        // 2. Launch test
        spyReportingDataProcessing.runProcessMain(null, defaultDir, inDirectory, null);

        // 3. checks
        verify(spyReportingDataProcessing, times(1)).runProcess(any(), eq(inDirParam), eq(inDirParam), any());
    }


    @Test
    void runProcessGenesis_Test() throws KraftwerkException {
        // 1. Mock the dependencies
        spyReportingDataProcessing = Mockito.spy(new ReportingDataProcessing());
        doNothing().when(spyReportingDataProcessing).runProcess(any(), any(), any(), any());

        Mode mode = Mode.WEB;
        String defaultDir = "/a/b/c/defaultDir";
        String inDirectory = "inDir";
        Path inDirParam = Path.of(defaultDir, "in", mode.getFolder(), inDirectory);
        Path specDirectory = Path.of(defaultDir, "specs", inDirectory);

        // 2. Launch test
        spyReportingDataProcessing.runProcessGenesis(null, mode, defaultDir, inDirectory, null);

        // 3. checks
        verify(spyReportingDataProcessing, times(1)).runProcess(any(), eq(inDirParam), eq(specDirectory), any());
    }

    @Test
    void parseReportingData_exceotion1_Test() {
        // 1. Mock the dependencies
        ModeInputs mockModeInputs = mock(ModeInputs.class);
        XMLReportingDataParser mockXMLReportingDataParser = mock(XMLReportingDataParser.class);
        CSVReportingDataParser mockCSVReportingDataParser = mock(CSVReportingDataParser.class);
        spyReportingDataProcessing = Mockito.spy(new ReportingDataProcessing());
        Path path = Path.of("aaa.xyz");
        doReturn(path).when(mockModeInputs).getReportingDataFile();
        doReturn(mockXMLReportingDataParser).when(spyReportingDataProcessing).getXMLReportingDataParser(any());
        doReturn(mockCSVReportingDataParser).when(spyReportingDataProcessing).getCSVReportingDataParser(any());

        // 3. checks
        Throwable exception = assertThrows(KraftwerkException.class, () -> spyReportingDataProcessing.parseReportingData(mockModeInputs, null, null));
        assertEquals("Reporting data file aaa.xyz not found !", exception.getMessage());
    }


    @Test
    void parseReportingData_exceotion2_Test() {
        // 1. Mock the dependencies
        ModeInputs mockModeInputs = mock(ModeInputs.class);
        XMLReportingDataParser mockXMLReportingDataParser = mock(XMLReportingDataParser.class);
        CSVReportingDataParser mockCSVReportingDataParser = mock(CSVReportingDataParser.class);
        spyReportingDataProcessing = Mockito.spy(new ReportingDataProcessing());
        Path absolutePath = Path.of(new File("src/test/resources/" + "unit_tests/reportingdata/reportingdata_unittest.xyz").getAbsolutePath());
        doReturn(absolutePath).when(mockModeInputs).getReportingDataFile();
        doReturn(mockXMLReportingDataParser).when(spyReportingDataProcessing).getXMLReportingDataParser(any());
        doReturn(mockCSVReportingDataParser).when(spyReportingDataProcessing).getCSVReportingDataParser(any());

        // 3. checks
        Throwable exception = assertThrows(KraftwerkException.class, () -> spyReportingDataProcessing.parseReportingData(mockModeInputs, null, null));
        assertEquals("Reporting data file path must be a xml or csv file ! Got " + absolutePath, exception.getMessage());
    }


    @Test
    void parseReportingData_XML_Test() throws KraftwerkException {
        // 1. Mock the dependencies
        ModeInputs mockModeInputs = mock(ModeInputs.class);
        XMLReportingDataParser mockXMLReportingDataParser = mock(XMLReportingDataParser.class);
        CSVReportingDataParser mockCSVReportingDataParser = mock(CSVReportingDataParser.class);
        spyReportingDataProcessing = Mockito.spy(new ReportingDataProcessing());
        Path absolutePath = Path.of(new File("src/test/resources/" + "unit_tests/reportingdata/reportingdata_unittest.xml").getAbsolutePath());
        doReturn(absolutePath).when(mockModeInputs).getReportingDataFile();
        doReturn(mockXMLReportingDataParser).when(spyReportingDataProcessing).getXMLReportingDataParser(any());
        doReturn(mockCSVReportingDataParser).when(spyReportingDataProcessing).getCSVReportingDataParser(any());

        // 2. Launch test
        spyReportingDataProcessing.parseReportingData(mockModeInputs, null, null);

        // 3. checks
        verify(mockXMLReportingDataParser, times(1)).parseReportingData(any(), any(), eq(true));
        verify(mockCSVReportingDataParser, times(0)).parseReportingData(any(), any(), eq(true));
    }


    @Test
    void parseReportingData_CSV_Test() throws KraftwerkException {
        // 1. Mock the dependencies
        ModeInputs mockModeInputs = mock(ModeInputs.class);
        XMLReportingDataParser mockXMLReportingDataParser = mock(XMLReportingDataParser.class);
        CSVReportingDataParser mockCSVReportingDataParser = mock(CSVReportingDataParser.class);
        spyReportingDataProcessing = Mockito.spy(new ReportingDataProcessing());
        Path absolutePath = Path.of(new File("src/test/resources/" + "unit_tests/reportingdata/reportingdata_unittest.csv").getAbsolutePath());
        doReturn(absolutePath).when(mockModeInputs).getReportingDataFile();
        doReturn(mockXMLReportingDataParser).when(spyReportingDataProcessing).getXMLReportingDataParser(any());
        doReturn(mockCSVReportingDataParser).when(spyReportingDataProcessing).getCSVReportingDataParser(any());

        // 2. Launch test
        spyReportingDataProcessing.parseReportingData(mockModeInputs, null, null);

        // 3. checks
        verify(mockXMLReportingDataParser, times(0)).parseReportingData(any(), any(), eq(true));
        verify(mockCSVReportingDataParser, times(1)).parseReportingData(any(), any(), eq(true));
    }


}
