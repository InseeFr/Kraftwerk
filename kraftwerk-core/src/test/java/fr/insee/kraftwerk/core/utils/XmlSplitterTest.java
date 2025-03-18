package fr.insee.kraftwerk.core.utils;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.xml.XmlSplitter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class XmlSplitterTest {

	static final String OUT_DIRECTORY = TestConstants.UNIT_TESTS_DUMP+"/split/";

	static final FileUtilsInterface fileUtilsInterface = new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY);

	@BeforeAll
	static void cleanUpBeforeTests() throws Exception {
		File file = new File(OUT_DIRECTORY);
		if (file.exists()) {
			List<String> splitFiles = fileUtilsInterface.listFileNames(OUT_DIRECTORY);
			if (splitFiles != null){
				for (String splitFile : splitFiles) {
					if(!Files.deleteIfExists(Paths.get(OUT_DIRECTORY+splitFile))){
						System.out.println("File "+splitFile+" not deleted");
					}
				}
			}
		}
		XmlSplitter.split(TestConstants.UNIT_TESTS_DIRECTORY+"/data/lunatic_xml/", "fake-lunatic-data-1.xml",
				OUT_DIRECTORY,"SurveyUnit",2, new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY));
	}

	@Test
	@DisplayName("OutDirectory should contain 3 files")
	void splitInThreeTest() {
		List<String> splitFiles = fileUtilsInterface.listFileNames(OUT_DIRECTORY);
		Assertions.assertEquals(3, splitFiles.size());
	}

	@Test
	@DisplayName("File split1.xml file should contain 2 survey units")
	void contains2SuTest() throws XMLStreamException, IOException {
		int count = 0;
		XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLEventReader eventReader = factory.createXMLEventReader(new FileInputStream(OUT_DIRECTORY+"split1.xml"));
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					if (startElement.getName().getLocalPart().equals("SurveyUnit")) {
						count++;
					}
				}
			}

		Assertions.assertEquals(2, count);
	}


}
