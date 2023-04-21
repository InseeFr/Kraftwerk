package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.parsers.DataFormat;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.extern.log4j.Log4j2;



@Log4j2
public class DataProcessingManager {

	/**
	 * Return the data processing class adapted to the data collection tool given.
	 *
	 * @param dataFormat One of the DataFormat.
	 * @param variablesMap 
	 *
	 * @return One of the concrete parsers.
	 */
	public static UnimodalDataProcessing getProcessingClass(DataFormat dataFormat, VtlBindings vtlBindings, VariablesMap variablesMap) {
		UnimodalDataProcessing dataProcessing = null;
		switch (dataFormat) {
		case XFORMS:
			dataProcessing = new XformsDataProcessing(vtlBindings, variablesMap);
			break;
		case PAPER:
			dataProcessing = new PaperDataProcessing(vtlBindings, variablesMap);
			break;
		case LUNATIC_XML:
		case LUNATIC_JSON:
			dataProcessing = new LunaticDataProcessing(vtlBindings, variablesMap);
			break;
		default:
			log.warn("Unknown data collection tool, null returned.");
			break;
		}
		return dataProcessing;
	}
}
