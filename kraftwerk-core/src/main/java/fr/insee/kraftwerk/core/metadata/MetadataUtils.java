package fr.insee.kraftwerk.core.metadata;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.SpecType;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.reader.ReaderUtils;
import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
public class MetadataUtils {

	private MetadataUtils(){
		throw new IllegalStateException("Utility class");
	}

	public static Map<String, MetadataModel> getMetadata(Map<String, ModeInputs> modeInputsMap, FileUtilsInterface fileUtilsInterface){
		Map<String, MetadataModel> metadataModels = new LinkedHashMap<>();
		modeInputsMap.forEach((k, v) -> putToMetadataModels(k,v,metadataModels, fileUtilsInterface));
		return metadataModels;
	}

	private static void putToMetadataModels(String dataMode, ModeInputs modeInputs, Map<String, MetadataModel> metadataModels, FileUtilsInterface fileUtilsInterface) {
		// Step 1 : we add the variables read in the DDI and Lunatic if found
		try {
            InputStream ddiStream = fileUtilsInterface.readFile(modeInputs.getDdiUrl());
            InputStream lunaticStream = null;
            boolean hasLunatic = modeInputs.getLunaticFile() != null;

            if (hasLunatic) {
                lunaticStream = fileUtilsInterface.readFile(modeInputs.getLunaticFile().toString());
                log.info("Adding variables from Lunatic file: {}", modeInputs.getLunaticFile().getFileName());
            }

            MetadataModel metadataModel = ReaderUtils.getMetadataFromDDIAndLunatic(
                    modeInputs.getDdiUrl(),
                    ddiStream,
                    lunaticStream
            );
            if (hasLunatic) {
                // We read and store lunaticModelVersion
                metadataModel.putSpecVersions(SpecType.LUNATIC,LunaticReader.getLunaticModelVersion(lunaticStream));
            }
            // Step 3 : we add reporting data group if there is any reporting data
            if(modeInputs.getReportingDataFile() != null){
                metadataModel.getGroups().put(Constants.REPORTING_DATA_GROUP_NAME, new Group(Constants.REPORTING_DATA_GROUP_NAME));
            }
            metadataModels.put(dataMode, metadataModel);

        } catch (MetadataParserException e) {
			log.error(e.getMessage());
		}

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
