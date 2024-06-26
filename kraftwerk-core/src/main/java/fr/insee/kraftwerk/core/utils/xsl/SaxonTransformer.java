package fr.insee.kraftwerk.core.utils.xsl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class SaxonTransformer {
	private FileUtilsInterface fileUtilsInterface;

	/**
	 * XSL transformation method using Saxon.
	 *
	 * @param inputXmlPath   : Path of the XML input file
	 * @param inputXslPath  : Path to the XSL file from the resources folder of the
	 *                      application
	 * @param outputXmlPath : Path to the XML output file which will be created
	 */
	public void xslTransform(String inputXmlPath, String inputXslPath, Path outputXmlPath) {
		log.info("About to transform the file: " + inputXmlPath);
		log.info("using the XSL file " + inputXslPath);

		// Get the XML input file
		StreamSource xmlSource;
		InputStream xmlInput;
		xmlInput = fileUtilsInterface.readFile(inputXmlPath);
		xmlSource = new StreamSource(xmlInput);
		xmlSource.setSystemId(inputXmlPath);

		// Get the XSL file
		StreamSource xslSource;
		InputStream xslInput;
		xslInput = SaxonTransformer.class.getClassLoader().getResourceAsStream(inputXslPath);
		xslSource = new StreamSource(xslInput);
		xslSource.setSystemId(inputXslPath);

		// Instantiation of the XSL transformer factory
		TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl(); // basic transformer
		transformerFactory.setURIResolver(new ClasspathURIResolver());

		// Apply the XSL transformation
		try {
			Transformer transformer = transformerFactory.newTransformer(xslSource);
			StreamResult sr = new StreamResult(outputXmlPath.toFile());
			transformer.transform(xmlSource, sr);
		} catch (TransformerConfigurationException e) {
			log.error("Error when trying to configure the XSL transformer using XSL file: " + inputXslPath, e);
		} catch (TransformerException e) {
			log.error("Error when trying to apply the XSL transformation using XSL file: " + inputXslPath, e);
		}

		try {
			xmlInput.close();
			if(xslInput != null){
				xslInput.close();
			}
		} catch (IOException | NullPointerException e ) {
			log.error("IOException occurred when trying to close the streams after XSL transformation.", e);
		}
	}

	/**
	 * XSL transformation method using Saxon.
	 *
	 * @param inputXmlPath  : Path to the XML input file
	 * @param inputXslPath  : Path to the XSL file from the resources' folder of the
	 *                      application
	 * @param outputXmlPath : Path to the XML output file which will be created
	 */
	public void xslTransform(Path inputXmlPath, String inputXslPath, Path outputXmlPath) {
		log.info("About to transform the file " + inputXmlPath);
		log.info("using the XSL file " + inputXslPath);

		xslTransform(inputXmlPath.toString(), inputXslPath, outputXmlPath);
	}
}