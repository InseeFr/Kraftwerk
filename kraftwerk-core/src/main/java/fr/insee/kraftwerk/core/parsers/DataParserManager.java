package fr.insee.kraftwerk.core.parsers;

import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import lombok.extern.log4j.Log4j2;

/**
 * Factory class for data parsers.
 */
@Log4j2
public class DataParserManager {
	
	/**
	 * Return the parser adapted to read data from the data collection tool given.
	 *
	 * @param dataFormat
	 * One of the DataFormat.
	 *
	 * @return
	 * One of the concrete parsers.
	 */
	public static DataParser getParser(DataFormat dataFormat, SurveyRawData data) {
		switch (dataFormat) {
			case XFORMS:
				return new XformsDataParser(data);
			case PAPER:
				return new PaperDataParser(data);
			case LUNATIC_XML:
				return new LunaticXmlDataParser(data);
			case LUNATIC_JSON:
				return new LunaticJsonDataParser(data);
			default:
				log.debug(String.format("Unknown data format: %s", dataFormat));
				throw new IllegalArgumentException(String.format("Unknown data format: %s", dataFormat));
		}
	}

}
