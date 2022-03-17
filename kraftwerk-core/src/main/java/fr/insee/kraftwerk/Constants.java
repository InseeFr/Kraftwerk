package fr.insee.kraftwerk;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;


/**
 * This class contains paths and fixed parameters used in the application.
 */
@Slf4j
public final class Constants {

	private Constants() {}

	public static final String USER_INPUT_FILE = "kraftwerk.json";

	// ----- Main resources folders
	public static final String RESOURCES_FOLDER = "src/main/resources";
	public static final String XSLT_FOLDER = RESOURCES_FOLDER + "/xslt";
	public static final String XSLT_FOLDER_PATH = RESOURCES_FOLDER + XSLT_FOLDER;
	public static final String VTL_FOLDER_PATH = RESOURCES_FOLDER + "/vtl";

	// ----- XSL scripts
	public static final String XSLT_STRUCTURED_VARIABLES = "xslt/structured-variables.xsl";

	// ----- Batch parameters from properties
	public static final String METADATA_SEPARATOR = ".";
	public static final char CSV_PAPER_DATA_SEPARATOR = '#'; //TODO: get it from properties
	public static final char CSV_REPORTING_DATA_SEPARATOR = ','; //TODO: get it from properties
	public static final char CSV_OUTPUTS_SEPARATOR = ';'; //TODO: get it from properties
	public static final char CSV_OUTPUTS_QUOTE_CHAR = '"'; //TODO: get it from properties

	// ----- Batch fixed parameters
	public static final String ROOT_GROUP_NAME = "RACINE";
	public static final String ROOT_IDENTIFIER_NAME = "IdUE";

	// ----- Explicit Variables Names
	public static final String MODE_VARIABLE_NAME = "MODE_KRAFTWERK";
	public static final String LENGTH_ORCHESTRATORS_NAME = "DURATION_ACTIVE_SESSION";
	public static final String NUMBER_ORCHESTRATORS_NAME = "NB_ORCHESTRATORS";
	public static final String STATE_SUFFIX_NAME = "STATE";
	public static final String LAST_STATE_NAME = "LAST_" + Constants.STATE_SUFFIX_NAME;
	public static final String PARADATA_VARIABLES_PREFIX = "CHANGES_";


	
	// ---------- Functions
	// ---------- Get a file
	/**
	 * Example: getResourceAbsolutePath("sample_data/hello.csv");
	 *
	 * @param resourceName the name of the resource
	 */
	public static String getResourceAbsolutePath(String resourceName) {
		File file = new File(resourceName);
		return file.getAbsolutePath();
	}
	

	/** Update path with the root
     *
     * @param inputDirectory the directory where all files are stored
     *
     * @param campaignName the name of the campaign
     *
     * @param userPath the path to the file or the folder
     * 
	 * @return the file or null when not found.
	 * */
    public static String getInputPath(String inputDirectory, String campaignName, String userPath) {
        if (userPath != null) {
            return inputDirectory + "/" + campaignName + "/" + userPath;
        } else {
            return null;
        }
    }

	/** Update path with the root.
	 *
	 * @param inputDirectory the directory where all files are stored
	 * @param userPath the path to the file or the folder
	 *
	 * @return the file or null when not found.
	 * */
	public static String getInputPath(Path inputDirectory, String userPath) {
		if (userPath != null) {
			return inputDirectory.resolve(userPath).toString();
		} else {
			return null;
		}

	}

	/** Generic file getter from classpath
	 *
	 * @param path the path to the file or the folder
	 *
	 * @return the file or null when not found.
	 * */
	public static InputStream getInputStreamFromPath(String path) {
		log.debug("Loading " + path);
		try {
			return Constants.class.getResourceAsStream(path);
		} catch (Exception e) {
			log.error("Error when loading file");
			return null;
		}
	}
		
	/** Generic getter for files
	 * @param path the path to the file or the folder
	 */
	public static File getFileFromPath(String path) {
		return Paths.get(path).toFile();
	}

	// ---------- Parse a file
	/** Parse JSON from fileName
	 * @param filename
	 */
	public static Object readJsonSimple(String filename) throws Exception {
		FileReader reader = new FileReader(filename);
		JSONParser jsonParser = new JSONParser();
		return jsonParser.parse(reader);
	}

	// ---------- Maths function
	/** Convert a long into a text value standardized
	 * @param datelong
	 */
	public static String convertToDateFormat(long datelong) throws Exception {
		return String.format("%2d jours, %02d:%02d:%02d", TimeUnit.MILLISECONDS.toDays(datelong),
				TimeUnit.MILLISECONDS.toHours(datelong) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(datelong)),
				TimeUnit.MILLISECONDS.toMinutes(datelong)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(datelong)),
				TimeUnit.MILLISECONDS.toSeconds(datelong)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(datelong)))
				.trim();
	}

	/** Convert a string path to an URL object.
	 *
	 * @param filePath
	 * Can be a URL or a local absolute path.
	 *
	 * @return
	 * A java.net.URL object.
	 *
	 * @throws MalformedURLException if the path given is incorrect.
	 * */
	public static URL convertToUrl(String filePath) throws MalformedURLException {
		if(filePath.startsWith("http")) {
			return new URL(filePath);
		} else {
			return new File(filePath).toURI().toURL();
		}
	}
		
}
