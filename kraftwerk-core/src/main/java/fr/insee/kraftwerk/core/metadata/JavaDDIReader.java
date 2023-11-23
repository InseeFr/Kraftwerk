package fr.insee.kraftwerk.core.metadata;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import fr.insee.kraftwerk.core.exceptions.DDIParsingException;
import io.github.nsenave.ddi.lifecycle33.instance.DDIInstanceDocument;
import io.github.nsenave.ddi.lifecycle33.instance.DDIInstanceType;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.xsl.SaxonTransformer;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JavaDDIReader implements DDIReader {

    /**
     * Parses the DDI from the given URL, and store its variables in a VariablesMap object.
     * @param ddiUrl URL to a DDI file.
     * @return A VariablesMap object containing information from the given DDI URL.
     * @throws DDIParsingException if an error occurs during parsing the file located by the URL.
     */
    public VariablesMap getVariablesFromDDI(URL ddiUrl) throws DDIParsingException {

        try {
            DDIInstanceDocument ddiInstanceDocument = DDIInstanceDocument.Factory.parse(ddiUrl);
            return readVariables(ddiInstanceDocument.getDDIInstance());
        }

        catch (XmlException e) {
            throw new DDIParsingException("Error when parsing DDI file from URL: " + ddiUrl, e);
        }
        catch (IOException e) {
            throw new DDIParsingException("Unable to read DDI file from URL: " + ddiUrl, e);
        }
    }

    /**
     * Return a VariablesMap containing variables from the DDI given.
     * @param ddiInstanceType DDI instance object.
     * @return A VariablesMap containing variables from the DDI given.
     */
    private static VariablesMap readVariables(DDIInstanceType ddiInstanceType) throws IOException {
        VariablesMap variablesMap = new VariablesMap();

        // wip

        return variablesMap;
    }

}
