package fr.insee.kraftwerk.core.parsers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataParserManagerTest {

    @Test
    public void testGetParser() {
        assertTrue(DataParserManager.getParser(DataFormat.XFORMS) instanceof XformsDataParser);
        assertTrue(DataParserManager.getParser(DataFormat.LUNATIC_XML) instanceof LunaticXmlDataParser);
        assertTrue(DataParserManager.getParser(DataFormat.LUNATIC_JSON) instanceof LunaticJsonDataParser);
        assertTrue(DataParserManager.getParser(DataFormat.PAPER) instanceof PaperDataParser);
    }
}
