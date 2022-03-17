package fr.insee.kraftwerk.parsers;

import lombok.extern.slf4j.Slf4j;

/**
 * Factory class for data parsers.
 */
@Slf4j
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
	public static DataParser getParser(DataFormat dataFormat) {
		switch (dataFormat) {
			case XFORMS:
				return new XformsDataParser();
			case PAPER:
				return new PaperDataParser();
			case LUNATIC_XML:
				return new LunaticXmlDataParser();
			case LUNATIC_JSON:
				return new LunaticJsonDataParser();
			default:
				log.debug(String.format("Unknown data format: %s", dataFormat));
				throw new IllegalArgumentException(String.format("Unknown data format: %s", dataFormat));
		}
	}

}
