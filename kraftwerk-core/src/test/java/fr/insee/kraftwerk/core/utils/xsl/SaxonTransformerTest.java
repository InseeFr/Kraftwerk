package fr.insee.kraftwerk.core.utils.xsl;

import java.net.MalformedURLException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.utils.TextFileReader;

class SaxonTransformerTest  {

    @Test
    void applyXsltScript() throws MalformedURLException {
        String xsltTestScript = TestConstants.UNIT_TESTS_DIRECTORY + "/utils/xsl/do-nothing.xsl";
        String inXmlFile = TestConstants.UNIT_TESTS_DIRECTORY + "/utils/xsl/note.xml";
        String outXmlFile = TestConstants.UNIT_TESTS_DUMP + "/xsl-output.xml";
        //
        SaxonTransformer saxonTransformer = new SaxonTransformer();
        saxonTransformer.xslTransform(Path.of(inXmlFile), xsltTestScript,Path.of(outXmlFile));
        //
        String inContent = TextFileReader.readFromPath(Path.of(inXmlFile));
        String outContent = TextFileReader.readFromPath(Path.of(outXmlFile));
        //
        XmlAssert.assertThat(inContent).and(outContent).areSimilar();
    }
}
