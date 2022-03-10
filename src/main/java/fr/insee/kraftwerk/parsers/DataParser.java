package fr.insee.kraftwerk.parsers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.kraftwerk.rawdata.SurveyRawData;

/**
 * Interface for concrete parsers.
 */
public interface DataParser {

	/**
	 * Read a survey data file and fills the SurveyRawData object with it.
	 *
	 * @param data
	 * A SurveyRawData object, the variables must have already been set.
	 *
	 */
	public void parseSurveyData(SurveyRawData data);

}
