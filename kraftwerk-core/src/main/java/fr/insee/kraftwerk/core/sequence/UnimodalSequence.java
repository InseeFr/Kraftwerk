package fr.insee.kraftwerk.core.sequence;

import fr.insee.bpm.metadata.model.CalculatedVariables;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.model.VariablesMap;

import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.dataprocessing.*;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.*;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.util.Map;

@NoArgsConstructor
@Log4j2
public class UnimodalSequence {

	public void applyUnimodalSequence(UserInputs userInputs, String dataMode, VtlBindings vtlBindings,
									  KraftwerkExecutionContext kraftwerkExecutionContext, Map<String, MetadataModel> metadataModels, FileUtilsInterface fileUtilsInterface) {
		ModeInputs modeInputs = userInputs.getModeInputs(dataMode);
		String vtlGenerate;

		/* Step 2.4a : Check incoherence between expected variables' length and actual length received */
		VariablesMap variablesMap = metadataModels.get(dataMode).getVariables();
		for (String variableName : variablesMap.getVariableNames()){
			Variable variable = variablesMap.getVariable(variableName);
			if (variable.getSasFormat() != null && variable.getExpectedLength()<variable.getMaxLengthData() && variable.getType() != VariableType.BOOLEAN){
				log.warn(String.format("%s expected length is %s but max length received is %d",variable.getName(),variable.getExpectedLength(), variable.getMaxLengthData()));
				ErrorVariableLength error = new ErrorVariableLength(variable, dataMode);
				if (!kraftwerkExecutionContext.getErrors().contains(error)){
					kraftwerkExecutionContext.getErrors().add(error);
				}
			}
		}

		/* Step 2.4b : Apply VTL expression for calculated variables (if any) */
		if (modeInputs.getLunaticFile() != null) {
			CalculatedVariables calculatedVariables = LunaticReader
					.getCalculatedFromLunatic(fileUtilsInterface.readFile(modeInputs.getLunaticFile().toString()));
			CalculatedProcessing calculatedProcessing = new CalculatedProcessing(vtlBindings, calculatedVariables, fileUtilsInterface);
			vtlGenerate = calculatedProcessing.applyCalculatedVtlTransformations(dataMode, modeInputs.getModeVtlFile(), kraftwerkExecutionContext);
			TextFileWriter.writeFile(fileUtilsInterface.getTempVtlFilePath(userInputs, "CalculatedProcessing", dataMode),
					vtlGenerate, fileUtilsInterface);

		} else {
			log.info(String.format("No Lunatic questionnaire file for mode \"%s\". Calculated variables will not be evaluated", dataMode));
		}

		/* Step 2.4c : Prefix variable names with their belonging group names */
		vtlGenerate = new GroupProcessing(vtlBindings, metadataModels.get(dataMode), fileUtilsInterface).applyVtlTransformations(dataMode, null, kraftwerkExecutionContext);
		TextFileWriter.writeFile(fileUtilsInterface.getTempVtlFilePath(userInputs, "GroupProcessing", dataMode), vtlGenerate, fileUtilsInterface);

		/* Step 2.5 : Apply standard mode-specific VTL transformations */
		UnimodalDataProcessing dataProcessing = DataProcessingManager.getProcessingClass(modeInputs.getDataFormat(),
				vtlBindings, metadataModels.get(dataMode), fileUtilsInterface);
		vtlGenerate = dataProcessing.applyVtlTransformations(
				dataMode,
				Path.of(Constants.VTL_FOLDER_PATH)
						.resolve("unimode")
						.resolve(dataMode+".vtl"),
				kraftwerkExecutionContext);
		TextFileWriter.writeFile(fileUtilsInterface.getTempVtlFilePath(userInputs, "StandardVtl", dataMode), vtlGenerate, fileUtilsInterface);

		/* Step 2.5b : Apply TCM VTL transformations */
		TCMSequencesProcessing tcmSequencesProcessing = new TCMSequencesProcessing(vtlBindings,metadataModels.get(dataMode) , Constants.VTL_FOLDER_PATH, fileUtilsInterface);
		vtlGenerate = tcmSequencesProcessing.applyAutomatedVtlInstructions(dataMode, kraftwerkExecutionContext);
		TextFileWriter.writeFile(fileUtilsInterface.getTempVtlFilePath(userInputs, "TCMSequenceVTL", dataMode), vtlGenerate, fileUtilsInterface);

		/* Step 2.5c : Apply user specified mode-specific VTL transformations */
		vtlGenerate = dataProcessing.applyVtlTransformations(dataMode, modeInputs.getModeVtlFile(), kraftwerkExecutionContext);
		TextFileWriter.writeFile(fileUtilsInterface.getTempVtlFilePath(userInputs, dataProcessing.getStepName(), dataMode),
				vtlGenerate, fileUtilsInterface);

	}

}
