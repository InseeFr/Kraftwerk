package fr.insee.kraftwerk.core.parsers;

import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;

/**
 * Factory class for data parsers.
 */
@Log4j2
public class DataParserManager {

	private DataParserManager(){
		throw new IllegalStateException("Utility class");
	}
	
	/**
	 * Return the parser adapted to read data from the data collection tool given.
	 *
	 * @param dataFormat
	 * One of the DataFormat.
	 *
	 * @return
	 * One of the concrete parsers.
	 */
	public static DataParser getParser(DataFormat dataFormat, SurveyRawData data, FileUtilsInterface fileUtilsInterface) {
		return switch (dataFormat) {
			case XFORMS -> new XformsDataParser(data, fileUtilsInterface);
			case PAPER -> new PaperDataParser(data, fileUtilsInterface);
			case LUNATIC_XML -> new LunaticXmlDataParser(data, fileUtilsInterface);
			case LUNATIC_JSON -> new LunaticJsonDataParser(data,fileUtilsInterface);
			default -> {
				log.debug(String.format("Unknown data format: %s", dataFormat));
				throw new IllegalArgumentException(String.format("Unknown data format: %s", dataFormat));
			}
		};
	}

}
