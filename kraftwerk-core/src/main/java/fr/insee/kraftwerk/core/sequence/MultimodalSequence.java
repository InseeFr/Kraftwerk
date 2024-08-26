package fr.insee.kraftwerk.core.sequence;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.dataprocessing.CleanUpProcessing;
import fr.insee.kraftwerk.core.dataprocessing.DataProcessing;
import fr.insee.kraftwerk.core.dataprocessing.InformationLevelsProcessing;
import fr.insee.kraftwerk.core.dataprocessing.MultimodeTransformations;
import fr.insee.kraftwerk.core.dataprocessing.ReconciliationProcessing;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public class MultimodalSequence {
	
	public void multimodalProcessing(UserInputs userInputs, VtlBindings vtlBindings, KraftwerkExecutionContext kraftwerkExecutionContext, Map<String, MetadataModel> metadataModels, FileUtilsInterface fileUtilsInterface) {
		String multimodeDatasetName = Constants.MULTIMODE_DATASET_NAME;

		/* Step 3.1 : aggregate unimodal datasets into a multimodal unique dataset */
		DataProcessing reconciliationProcessing = new ReconciliationProcessing(vtlBindings, fileUtilsInterface);
		String vtlGenerate = reconciliationProcessing.applyVtlTransformations(multimodeDatasetName,
				userInputs.getVtlReconciliationFile(), kraftwerkExecutionContext);
		TextFileWriter.writeFile(fileUtilsInterface.getTempVtlFilePath(userInputs, "ReconciliationProcessing",multimodeDatasetName), vtlGenerate, fileUtilsInterface);

		/* Step 3.1.b : clean up processing */
		CleanUpProcessing cleanUpProcessing = new CleanUpProcessing(vtlBindings, metadataModels, fileUtilsInterface);
		vtlGenerate = cleanUpProcessing.applyVtlTransformations(multimodeDatasetName, null, kraftwerkExecutionContext);
		TextFileWriter.writeFile(fileUtilsInterface.getTempVtlFilePath(userInputs, "CleanUpProcessing",multimodeDatasetName), vtlGenerate, fileUtilsInterface);

		/* Step 3.2 : treatments on the multimodal dataset */
		DataProcessing multimodeTransformations = new MultimodeTransformations(vtlBindings, fileUtilsInterface);
		vtlGenerate = multimodeTransformations.applyVtlTransformations(multimodeDatasetName,
				userInputs.getVtlTransformationsFile(), kraftwerkExecutionContext);
		TextFileWriter.writeFile(fileUtilsInterface.getTempVtlFilePath(userInputs, "MultimodeTransformations",multimodeDatasetName), vtlGenerate, fileUtilsInterface);

		/* Step 3.3 : create datasets on each information level (i.e. each group) */
		DataProcessing informationLevelsProcessing = new InformationLevelsProcessing(vtlBindings, fileUtilsInterface);
		vtlGenerate = informationLevelsProcessing.applyVtlTransformations(multimodeDatasetName,
				userInputs.getVtlInformationLevelsFile(), kraftwerkExecutionContext);
		TextFileWriter.writeFile(fileUtilsInterface.getTempVtlFilePath(userInputs, "InformationLevelsProcessing",multimodeDatasetName), vtlGenerate, fileUtilsInterface);
	}
}
