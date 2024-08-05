package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.bpm.metadata.model.MetadataModel;

import fr.insee.kraftwerk.core.parsers.DataFormat;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.extern.log4j.Log4j2;



@Log4j2
public class DataProcessingManager {

	private DataProcessingManager() {
		//Utility class
	}

	/**
	 * Return the data processing class adapted to the data collection tool given.
	 *
	 * @param dataFormat One of the DataFormat.
	 * @param metadataModel metadataModel
	 *
	 * @return One of the concrete parsers.
	 */
	public static UnimodalDataProcessing getProcessingClass(DataFormat dataFormat, VtlBindings vtlBindings, MetadataModel metadataModel, FileUtilsInterface fileUtilsInterface) {
		UnimodalDataProcessing dataProcessing = null;
		switch (dataFormat) {
		case XFORMS:
			dataProcessing = new XformsDataProcessing(vtlBindings, metadataModel, fileUtilsInterface);
			break;
		case PAPER:
			dataProcessing = new PaperDataProcessing(vtlBindings, metadataModel, fileUtilsInterface);
			break;
		case LUNATIC_XML , LUNATIC_JSON:
			dataProcessing = new LunaticDataProcessing(vtlBindings, metadataModel, fileUtilsInterface);
			break;
		default:
			log.warn("Unknown data collection tool, null returned.");
			break;
		}
		return dataProcessing;
	}
}
