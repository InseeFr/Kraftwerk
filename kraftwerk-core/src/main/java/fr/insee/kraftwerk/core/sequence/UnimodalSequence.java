package fr.insee.kraftwerk.core.sequence;

import java.util.List;
import java.util.Map;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.dataprocessing.CalculatedProcessing;
import fr.insee.kraftwerk.core.dataprocessing.DataProcessing;
import fr.insee.kraftwerk.core.dataprocessing.DataProcessingManager;
import fr.insee.kraftwerk.core.dataprocessing.GroupProcessing;
import fr.insee.kraftwerk.core.dataprocessing.UnimodalDataProcessing;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.*;
import fr.insee.kraftwerk.core.parsers.DataFormat;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@NoArgsConstructor
@Log4j2
public class UnimodalSequence {

	public void unimodalProcessing(UserInputs userInputs, String dataMode, VtlBindings vtlBindings,
								   List<KraftwerkError> errors, Map<String, VariablesMap> metadataVariables) {
		ModeInputs modeInputs = userInputs.getModeInputs(dataMode);
		String vtlGenerate;

		/* Step 2.4a : Check incoherence between expected variables' length and actual length received */
		VariablesMap variablesMap = metadataVariables.get(dataMode);
		for (String variableName : variablesMap.getVariableNames()){
			Variable variable = variablesMap.getVariable(variableName);
			if (!(variable.getSasFormat() == null) && variable.getExpectedLength()<variable.getMaxLengthData() && !(variable.getType() == VariableType.BOOLEAN)){
				log.warn(String.format("%s expected length is %s but max length received is %d",variable.getName(),variable.getExpectedLength(), variable.getMaxLengthData()));
				ErrorVariableLength error = new ErrorVariableLength(variable, dataMode);
				if (!errors.contains(error)){
					errors.add(error);
				}
			}
		}

		/* Step 2.4b : Apply VTL expression for calculated variables (if any) */
		if (modeInputs.getLunaticFile() != null) {
			CalculatedVariables calculatedVariables = LunaticReader
					.getCalculatedFromLunatic(modeInputs.getLunaticFile());
			DataProcessing calculatedProcessing = new CalculatedProcessing(vtlBindings, calculatedVariables);
			vtlGenerate = calculatedProcessing.applyVtlTransformations(dataMode, null, errors);
			TextFileWriter.writeFile(FileUtils.getTempVtlFilePath(userInputs, "CalculatedProcessing", dataMode),
					vtlGenerate);

		} else {
			log.info(String.format("No Lunatic questionnaire file for mode \"%s\"", dataMode));
			if (modeInputs.getDataFormat() == DataFormat.LUNATIC_XML
					|| modeInputs.getDataFormat() == DataFormat.LUNATIC_JSON) {
				log.warn(String.format("Calculated variables for lunatic data of mode \"%s\" will not be evaluated.",
						dataMode));
			}
		}

		/* Step 2.4c : Prefix variable names with their belonging group names */
		vtlGenerate = new GroupProcessing(vtlBindings, variablesMap).applyVtlTransformations(dataMode, null, errors);
		TextFileWriter.writeFile(FileUtils.getTempVtlFilePath(userInputs, "GroupProcessing", dataMode), vtlGenerate);

		/* Step 2.5 : Apply mode-specific VTL transformations */
		UnimodalDataProcessing dataProcessing = DataProcessingManager.getProcessingClass(modeInputs.getDataFormat(),
				vtlBindings, variablesMap);
		vtlGenerate = dataProcessing.applyVtlTransformations(dataMode, modeInputs.getModeVtlFile(), errors);
		TextFileWriter.writeFile(FileUtils.getTempVtlFilePath(userInputs, dataProcessing.getStepName(), dataMode),
				vtlGenerate);

	}

}
