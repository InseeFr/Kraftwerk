package fr.insee.kraftwerk.core.utils.xsl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.utils.TextFileReader;

class SaxonTransformerTest  {

    FileUtilsInterface fileUtilsInterface = new FileSystemImpl();

    @Test
    void applyXsltScript() throws IOException {
        String xsltTestScript = TestConstants.UNIT_TESTS_DIRECTORY + "/utils/xsl/do-nothing.xsl";
        String inXmlFile = TestConstants.UNIT_TESTS_DIRECTORY + "/utils/xsl/note.xml";
        String outXmlFile = TestConstants.UNIT_TESTS_DUMP + "/xsl-output.xml";
        //
        SaxonTransformer saxonTransformer = new SaxonTransformer(fileUtilsInterface);
        saxonTransformer.xslTransform(Path.of(inXmlFile), xsltTestScript,Path.of(outXmlFile));
        //
        String inContent = TextFileReader.readFromPath(Path.of(inXmlFile), fileUtilsInterface);
        String outContent = TextFileReader.readFromPath(Path.of(outXmlFile), fileUtilsInterface);
        //
        XmlAssert.assertThat(inContent).and(outContent).areSimilar();
    }
}
