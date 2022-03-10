package fr.insee.kraftwerk.utils.xsl;

import fr.insee.kraftwerk.TestConstants;
import fr.insee.kraftwerk.utils.TextFileReader;
import org.junit.jupiter.api.Test;

import org.xmlunit.assertj3.XmlAssert;

public class SaxonTransformerTest  {

 /*   @Test
    public void applyXsltScript() {
        String xsltTestScript = TestConstants.TEST_UNIT_INPUT_DIRECTORY + "/utils/xsl/do-nothing.xsl";
        String inXmlFile = TestConstants.TEST_UNIT_INPUT_DIRECTORY + "/utils/xsl/note.xml";
        String outXmlFile = TestConstants.TEST_UNIT_OUTPUT_DIRECTORY + "/xsl-output.xml";
        //
        SaxonTransformer saxonTransformer = new SaxonTransformer();
        saxonTransformer.xslTransform(inXmlFile, xsltTestScript, outXmlFile);
        //
        String inContent = TextFileReader.readFromPath(inXmlFile);
        String outContent = TextFileReader.readFromPath(outXmlFile);
        //
        XmlAssert.assertThat(inContent).and(outContent).areSimilar();
    }*/
}
