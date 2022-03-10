package fr.insee.kraftwerk.dataprocessing;

import fr.insee.kraftwerk.parsers.DataFormat;

import fr.insee.kraftwerk.vtl.VtlBindings;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class DataProcessingManager {

	/**
	 * Return the data processing class adapted to the data collection tool given.
	 *
	 * @param dataFormat One of the DataFormat.
	 *
	 * @return One of the concrete parsers.
	 */
	public static UnimodalDataProcessing getProcessingClass(DataFormat dataFormat, VtlBindings vtlBindings) {
		UnimodalDataProcessing dataProcessing = null;
		switch (dataFormat) {
		case XFORMS:
			dataProcessing = new XformsDataProcessing(vtlBindings);
			break;
		case PAPER:
			dataProcessing = new PaperDataProcessing(vtlBindings);
			break;
		case LUNATIC_XML:
		case LUNATIC_JSON:
			dataProcessing = new LunaticDataProcessing(vtlBindings);
			break;
		default:
			log.warn("Unknown data collection tool, null returned.");
			break;
		}
		return dataProcessing;
	}
}
