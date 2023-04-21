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

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SaxonTransformer {

	/**
	 * XSL transformation method using Saxon.
	 *
	 * @param inputXmlURL   : URL of the XML input file
	 * @param inputXslPath  : Path to the XSL file from the resources folder of the
	 *                      application
	 * @param outputXmlPath : Path to the XML output file which will be created
	 */
	public void xslTransform(URL inputXmlURL, String inputXslPath, Path outputXmlPath) {
		log.info("About to transform the file from URL: " + inputXmlURL);
		log.info("using the XSL file " + inputXslPath);

		// Get the XML input file
		StreamSource xmlSource;
		InputStream xmlInput;
		try {
			xmlInput = inputXmlURL.openStream();
			xmlSource = new StreamSource(xmlInput);
			xmlSource.setSystemId(inputXmlURL.toString());
		} catch (IOException e) {
			log.error(String.format("IOException when trying to read file from URL: %s", inputXmlURL), e);
			return; // to break here if the xml input file is not found
		}

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
			xslInput.close();
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

		try {
			URL inputXmlUrl = inputXmlPath.toUri().toURL();
			xslTransform(inputXmlUrl, inputXslPath, outputXmlPath);
		} catch (MalformedURLException e) {
			log.error(String.format("Error when converting file path '%s' to an URL.", inputXmlPath), e);
		}

	}

}