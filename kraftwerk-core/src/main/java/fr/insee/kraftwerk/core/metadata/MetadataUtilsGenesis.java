package fr.insee.kraftwerk.core.metadata;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.reader.ReaderUtils;
import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;

import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
public class MetadataUtilsGenesis {

	private MetadataUtilsGenesis() {
		throw new IllegalStateException("Utility class");
	}

	public static Map<String, MetadataModel> getMetadata(Map<String, ModeInputs> modeInputsMap, FileUtilsInterface fileUtilsInterface) throws MetadataParserException {
		Map<String, MetadataModel> metadataModels = new LinkedHashMap<>();
		for (Map.Entry<String, ModeInputs> entry : modeInputsMap.entrySet()) {
			String k = entry.getKey();
			ModeInputs v = entry.getValue();
			putToMetadataVariable(k, v, metadataModels, fileUtilsInterface);
		}
		return metadataModels;
	}

	private static void putToMetadataVariable(String dataMode, ModeInputs modeInputsGenesis, Map<String, MetadataModel> metadataModels, FileUtilsInterface fileUtilsInterface) throws MetadataParserException {
		// we add the variables read in the DDI and lunatic
        MetadataModel metadataModel = ReaderUtils.getMetadataFromDDIAndLunatic(
                modeInputsGenesis.getDdiUrl(),
                fileUtilsInterface.readFile(modeInputsGenesis.getDdiUrl()),
                modeInputsGenesis.getLunaticFile() != null ? modeInputsGenesis.getLunaticFile().toString() : null
        );
        metadataModels.put(dataMode, metadataModel);


    }

	public static Map<String, MetadataModel> getMetadataFromLunatic(Map<String, ModeInputs> modeInputsMap, FileUtilsInterface fileUtilsInterface) {
		Map<String, MetadataModel> metadataModels = new LinkedHashMap<>();
		modeInputsMap.forEach((k, v) -> putToMetadataVariableFromLunatic(k,v,metadataModels, fileUtilsInterface));
		return metadataModels;
	}

	private static void putToMetadataVariableFromLunatic(String dataMode, ModeInputs modeInputs, Map<String, MetadataModel> metadataModels, FileUtilsInterface fileUtilsInterface) {
		MetadataModel metadataModel = LunaticReader.getMetadataFromLunatic(fileUtilsInterface.readFile(modeInputs.getLunaticFile().toString()));
		metadataModels.put(dataMode, metadataModel);
	}
}
