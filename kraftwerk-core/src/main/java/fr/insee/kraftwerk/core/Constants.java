package fr.insee.kraftwerk.core;

import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * This class contains paths and fixed parameters used in the application.
 */
@Log4j2
public final class Constants {

    private Constants() {}

	public static final String USER_INPUT_FILE = "kraftwerk.json";
	public static final String USER_VTL_INPUT_FILE = "kraftwerk-vtl.json";

	// ----- Main resources folders
	public static final String RESOURCES_FOLDER = "src/main/resources";
	public static final String XSLT_FOLDER = RESOURCES_FOLDER + "/xslt";
	public static final String XSLT_FOLDER_PATH = RESOURCES_FOLDER + XSLT_FOLDER;
	public static final String VTL_FOLDER_PATH = RESOURCES_FOLDER + "/vtl";
	public static final String PARADATA_FOLDER = "/PARADATA";
	public static final String REPORTING_DATA_FOLDER = "/REPORTING_DATA";

	// ----- XSL scripts
	public static final String XSLT_STRUCTURED_VARIABLES = "xslt/structured-variables.xsl";

	// ----- Parameters from properties
	public static final String METADATA_SEPARATOR = ".";
	public static final String PAPER_IDENTIFIER_SEPARATOR = "_";
	public static final char CSV_PAPER_DATA_SEPARATOR = '#';
	public static final char CSV_REPORTING_DATA_SEPARATOR = ',';
	public static final char CSV_OUTPUTS_SEPARATOR = ';';
	@Getter
	private static char csvOutputQuoteChar = '"';

	// ----- Fixed parameters
	public static final String ROOT_GROUP_NAME = "RACINE";
	public static final String ROOT_IDENTIFIER_NAME = "IdUE";
	public static final String LOOP_NAME_PREFIX = "BOUCLE";
	public static final String MULTIMODE_DATASET_NAME = "MULTIMODE";
	public static final String REPORTING_DATA_GROUP_NAME = "REPORTINGDATA";
	public static final String REPORTING_DATA_INTERVIEWER_ID_NULL_PLACEHOLDER = "NON_AFFECTE_";
	public static final String REPORTING_DATA_INPUT_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
	public static final String REPORTING_DATA_OUTPUT_DATE_FORMAT = "yyyy-MM-dd-hh-mm-ss";
	public static final String REPORTING_DATA_FOLLOWUP_OUTPUT_STRING = "RELANCE";
	public static final String END_LINE = "\n";
	public static final String OUTPUT_FOLDER_DATETIME_PATTERN = "yyyy_MM_dd_HH_mm_ss";
	public static final String ERRORS_FILE_NAME = "errors.txt";
	public static final String DUCKDB_URL = "jdbc:duckdb:";

	public static final int DB_CONNECTION_TRY_COUNT = 10;
	public static final String DDI_FILE_REGEX = "ddi[\\w,\\s-]+\\.xml";
	public static final String LUNATIC_FILE_REGEX = "lunatic[\\w,\\s-]+\\.json";


	// ----- Explicit Variables Names
	public static final String MODE_VARIABLE_NAME = "MODE_KRAFTWERK";
	public static final String FILTER_RESULT_PREFIX = "FILTER_RESULT_";
	public static final String MISSING_SUFFIX = "_MISSING";
	public static final String COLLECTED = "COLLECTED";
	private static final String[] ENO_VARIABLES = {"COMMENT_QE","COMMENT_UE","HEURE_REMPL","MIN_REMPL"};


	// ----- Paradata Variables Names
	public static final String LENGTH_ORCHESTRATORS_NAME = "DURATION_ACTIVE_ORCHESTRATORS";
	public static final String LENGTH_SESSIONS_NAME = "DURATION_ACTIVE_SESSIONS";
	public static final String START_SESSION_NAME = "DEBUT_SESSION";
	public static final String FINISH_SESSION_NAME = "FIN_SESSION";
	public static final String NUMBER_ORCHESTRATORS_NAME = "NB_ORCHESTRATORS";
	public static final String NUMBER_SESSIONS_NAME = "NB_SESSIONS";
	public static final String PARADATA_VARIABLES_PREFIX = "CHANGES_";
	public static final String PARADATA_TIMESTAMP_SUFFIX = "_LONG";
	public static final String SURVEY_VALIDATION_DATE_NAME = "DATE_COLLECTE";

	// ----- Paradata event name
	public static final String PARADATA_SURVEY_VALIDATION_EVENT_NAME = "agree-sending-modal-button-orchestrator-collect";



	// ----- Reporting Variables Names
	public static final String REPORTING_DATA_PREFIX_NAME = "Report_";
	public static final String STATE_SUFFIX_NAME = "STATE";
	public static final String COMMENT_PREFIX_NAME = "COMMENT";
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
	public static final String ADRESS_ID_STAT_INSEE = "IDSTATINSEE";
	public static final String LAST_ATTEMPT_DATE = "LAST_ATTEMPT_DATE";
	public static final String OUTCOME_DATE = "OUTCOME_DATE";
	public static final String IDENTIFICATION_NAME = "identification";
	public static final String ACCESS_NAME = "access";
	public static final String SITUATION_NAME = "situation";
	public static final String CATEGORY_NAME = "category";
	public static final String OCCUPANT_NAME = "occupant";

	public static final String OUTCOME_SPOTTING = "outcome_spotting";
	public static final String REPORTING_DATA_SURVEY_VALIDATION_NAME = REPORTING_DATA_PREFIX_NAME + SURVEY_VALIDATION_DATE_NAME;
	
	// ------ Pairwise variables
	
	public static final int MAX_LINKS_ALLOWED = 21;
	public static final String BOUCLE_PRENOMS = "BOUCLE_PRENOMS";
	public static final String LIEN = "LIEN_";
	public static final String LIENS = "LIENS";
	public static final String PAIRWISE_GROUP_NAME = "LIENS";
	public static final String SAME_AXIS_VALUE = "0";
	public static final String NO_PAIRWISE_VALUE = "99";
	
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
        log.debug("Loading {}", path);
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
	public static Object readJsonSimple(Path filename, FileUtilsInterface fileUtilsInterface) throws IOException, ParseException {
		try(InputStream inputStream = fileUtilsInterface.readFile(filename.toString())){
			JSONParser jsonParser = new JSONParser();
			return jsonParser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		}
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
	public static URL convertToUrl(String filePath) throws MalformedURLException, URISyntaxException {
		if(filePath.startsWith("http")) {
			return new URI(filePath).toURL();
		} else {
			return new File(filePath).toURI().toURL();
		}
	}

	public static void setCsvOutputQuoteChar(char csvOutputQuoteChar) {
		Constants.csvOutputQuoteChar = csvOutputQuoteChar;
	}

	public static String[] getEnoVariables() {
		return ENO_VARIABLES;
	}
}
