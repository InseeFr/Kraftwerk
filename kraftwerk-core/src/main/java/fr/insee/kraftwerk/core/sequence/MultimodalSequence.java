package fr.insee.kraftwerk.core.sequence;

import java.util.List;
import java.util.Map;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.dataprocessing.CleanUpProcessing;
import fr.insee.kraftwerk.core.dataprocessing.DataProcessing;
import fr.insee.kraftwerk.core.dataprocessing.InformationLevelsProcessing;
import fr.insee.kraftwerk.core.dataprocessing.MultimodeTransformations;
import fr.insee.kraftwerk.core.dataprocessing.ReconciliationProcessing;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MultimodalSequence {
	
	public void multimodalProcessing(UserInputs userInputs, VtlBindings vtlBindings, List<KraftwerkError> errors, Map<String, MetadataModel> metadataModels, FileUtilsInterface fileUtilsInterface) {
		String multimodeDatasetName = Constants.MULTIMODE_DATASET_NAME;

		/* Step 3.1 : aggregate unimodal datasets into a multimodal unique dataset */
		DataProcessing reconciliationProcessing = new ReconciliationProcessing(vtlBindings);
		String vtlGenerate = reconciliationProcessing.applyVtlTransformations(multimodeDatasetName,
				userInputs.getVtlReconciliationFile(), errors);
		TextFileWriter.writeFile(fileUtilsInterface.getTempVtlFilePath(userInputs, "ReconciliationProcessing",multimodeDatasetName), vtlGenerate);

		/* Step 3.1.b : clean up processing */
		CleanUpProcessing cleanUpProcessing = new CleanUpProcessing(vtlBindings, metadataModels);
		vtlGenerate = cleanUpProcessing.applyVtlTransformations(multimodeDatasetName, null, errors);
		TextFileWriter.writeFile(fileUtilsInterface.getTempVtlFilePath(userInputs, "CleanUpProcessing",multimodeDatasetName), vtlGenerate);

		/* Step 3.2 : treatments on the multimodal dataset */
		DataProcessing multimodeTransformations = new MultimodeTransformations(vtlBindings);
		vtlGenerate = multimodeTransformations.applyVtlTransformations(multimodeDatasetName,
				userInputs.getVtlTransformationsFile(), errors);
		TextFileWriter.writeFile(fileUtilsInterface.getTempVtlFilePath(userInputs, "MultimodeTransformations",multimodeDatasetName), vtlGenerate);

		/* Step 3.3 : create datasets on each information level (i.e. each group) */
		DataProcessing informationLevelsProcessing = new InformationLevelsProcessing(vtlBindings);
		vtlGenerate = informationLevelsProcessing.applyVtlTransformations(multimodeDatasetName,
				userInputs.getVtlInformationLevelsFile(), errors);
		TextFileWriter.writeFile(fileUtilsInterface.getTempVtlFilePath(userInputs, "InformationLevelsProcessing",multimodeDatasetName), vtlGenerate);



	}


}
