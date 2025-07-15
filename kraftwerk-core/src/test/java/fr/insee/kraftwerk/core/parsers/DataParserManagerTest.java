package fr.insee.kraftwerk.core.parsers;

import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DataParserManagerTest {

    private static Stream<Arguments> getParserParameterizedTests() {
        return Stream.of(
                Arguments.of(DataFormat.XFORMS, XformsDataParser.class),
                Arguments.of(DataFormat.PAPER, PaperDataParser.class),
                Arguments.of(DataFormat.LUNATIC_XML, LunaticXmlDataParser.class),
                Arguments.of(DataFormat.LUNATIC_JSON, LunaticJsonDataParser.class)
        );
    }


    @ParameterizedTest
    @MethodSource("getParserParameterizedTests")
    void kraftwerkServiceType_ParameterizedTests(DataFormat dataFormatParam, Class<?> cls) {
        SurveyRawData data = new SurveyRawData();
        DataParser dataParser = DataParserManager.getParser(dataFormatParam, data, null);
        assertInstanceOf(cls, dataParser);
    }

}
