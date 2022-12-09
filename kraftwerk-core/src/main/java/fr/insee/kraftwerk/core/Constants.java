package fr.insee.kraftwerk.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import lombok.extern.slf4j.Slf4j;

/**
 * This class contains paths and fixed parameters used in the application.
 */
@Slf4j
public final class Constants {

	private Constants() {}

	public static final String USER_INPUT_FILE = "kraftwerk.json";
	public static final String USER_VTL_INPUT_FILE = "kraftwerk-vtl.json";

	// ----- Main resources folders
	public static final String RESOURCES_FOLDER = "src/main/resources";
	public static final String XSLT_FOLDER = RESOURCES_FOLDER + "/xslt";
	public static final String XSLT_FOLDER_PATH = RESOURCES_FOLDER + XSLT_FOLDER;
	public static final String VTL_FOLDER_PATH = RESOURCES_FOLDER + "/vtl";

	// ----- XSL scripts
	public static final String XSLT_STRUCTURED_VARIABLES = "xslt/structured-variables.xsl";

	// ----- Parameters from properties
	public static final String METADATA_SEPARATOR = ".";
	public static final String PAPER_IDENTIFIER_SEPARATOR = "_"; //TODO: get it from properties
	public static final char CSV_PAPER_DATA_SEPARATOR = '#'; //TODO: get it from properties
	public static final char CSV_REPORTING_DATA_SEPARATOR = ','; //TODO: get it from properties
	public static final char CSV_OUTPUTS_SEPARATOR = ';'; //TODO: get it from properties
	private static char csvOutputQuoteChar = '"';

	// ----- Fixed parameters
	public static final String ROOT_GROUP_NAME = "RACINE";
	public static final String ROOT_IDENTIFIER_NAME = "IdUE";

	// ----- Explicit Variables Names
	public static final String MODE_VARIABLE_NAME = "MODE_KRAFTWERK";
	public static final String FILTER_RESULT_PREFIX = "FILTER_RESULT_";
	public static final String MISSING_SUFFIX = "_MISSING";


	// ----- Paradata Variables Names
	public static final String LENGTH_ORCHESTRATORS_NAME = "DURATION_ACTIVE_SESSION";
	public static final String START_SESSION_NAME = "DEBUT_SESSION";
	public static final String FINISH_SESSION_NAME = "FIN_SESSION";
	public static final String NUMBER_ORCHESTRATORS_NAME = "NB_ORCHESTRATORS";
	public static final String PARADATA_VARIABLES_PREFIX = "CHANGES_";


	// ----- Reporting Variables Names
	public static final String STATE_SUFFIX_NAME = "STATE";
	public static final String LAST_STATE_NAME = "LAST_" + Constants.STATE_SUFFIX_NAME;
	  
	  public static final String INTERVIEWER_ID_NAME = "IDENQ";
	  public static final String ORGANIZATION_UNIT_ID_NAME = "ORGANIZATION_UNIT_ID";
	  public static final String ADRESS_RGES_NAME = "RGES";
	  public static final String ADRESS_NUMFA_NAME = "NUMFA";
	  public static final String ADRESS_SSECH_NAME = "SSECH";
	  public static final String ADRESS_LE_NAME = "LE";
	  public static final String ADRESS_EC_NAME = "EC";
	  public static final String ADRESS_BS_NAME = "BS";
	  public static final String ADRESS_NOI_NAME = "NOI";
	  public static final String SURVEY_DATE_DAY_NAME = "JOURENQ";
	  public static final String SURVEY_DATE_MONTH_NAME = "MOISENQ";
	  public static final String SURVEY_DATE_YEAR_NAME = "ANNEENQ";
	  public static final String OUTCOME_NAME = "OUTCOME";
	  public static final String NUMBER_ATTEMPTS_NAME = "NUMBER_CONTACT_ATTEMPTS";
	  public static final String OUTCOME_ATTEMPT_SUFFIX_NAME = "ATTEMPT";
	
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
	 * @param path the path to the file or the folder
	 *
	 * @return the file or null when not found.
	 * */
	public static Path getInputPath(Path inputDirectory, String path) {
		if (path != null) {
			return Paths.get(inputDirectory.resolve(path).toString());
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
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static Object readJsonSimple(Path filename) throws IOException, ParseException {
		FileReader reader = new FileReader(filename.toString());
		JSONParser jsonParser = new JSONParser();
		return jsonParser.parse(reader);
	}

	// ---------- Maths function
	/** Convert a long into a text value standardized
	 * @param datelong
	 */
	public static String convertToDateFormat(long datelong) {
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
	public static URL convertToUrl(Path filePath) throws MalformedURLException {
		return filePath.toFile().toURI().toURL();
	}


	public static void setCsvOutputQuoteChar(char csvOutputQuoteChar) {
		Constants.csvOutputQuoteChar = csvOutputQuoteChar;
	}

	public static char getCsvOutputQuoteChar() {
		return csvOutputQuoteChar;
	}
}
