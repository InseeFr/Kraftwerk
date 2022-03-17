package fr.insee.kraftwerk.core.parsers;

import fr.insee.kraftwerk.core.rawdata.SurveyRawData;

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
