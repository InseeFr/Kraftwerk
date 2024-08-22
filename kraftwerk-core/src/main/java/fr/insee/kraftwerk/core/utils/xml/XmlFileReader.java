package fr.insee.kraftwerk.core.utils.xml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

/**
 * Class providing method to parse an XML document using the nu.xom library.
 * TODO: (if it seems necessary) replace XOM by something else.
 */
@Log4j2
public class XmlFileReader {

    private boolean xsdValidation = false;
    private final FileUtilsInterface fileUtilsInterface;

    public XmlFileReader(FileUtilsInterface fileUtilsInterface) {
        this.fileUtilsInterface = fileUtilsInterface;
    }

    public Document readXmlFile(Path filePath) {
        try (InputStream inputStream = fileUtilsInterface.readFile(filePath.toString())){
            if(inputStream == null){
                return null;
            }
            Builder parser = new Builder(xsdValidation);
            return parser.build(inputStream);
        } catch (ValidityException ex) {
            log.warn("XSD validation error.", ex);
            log.warn("See following INFO log for details.");
            // TODO: maybe write the following log in a file.
            for (int i = 0; i < ex.getErrorCount(); i++) {
                log.info(String.format("Line %d, column %d :", ex.getLineNumber(i), ex.getColumnNumber(i)));
                log.info(ex.getValidityError(i));
            }
            return null;
        } catch (ParsingException ex) {
            log.error("XML document is malformed.", ex);
            return null;
        } catch (IOException ex) {
            log.error(String.format("Could not connect to data file %s", filePath), ex);
            return null;
        }
    }
}
