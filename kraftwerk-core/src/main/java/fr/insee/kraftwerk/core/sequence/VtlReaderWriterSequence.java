package fr.insee.kraftwerk.core.sequence;

import java.io.File;
import java.nio.file.Path;

import fr.insee.kraftwerk.core.dataprocessing.StepEnum;
import fr.insee.kraftwerk.core.utils.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;


public class VtlReaderWriterSequence {

	VtlExecute vtlExecute;
	private static final String JSON = ".json";


	public VtlReaderWriterSequence() {
		vtlExecute = new VtlExecute();
	}

	public void readDataset(String path,String bindingName, StepEnum previousStep, VtlBindings vtlBindings) {
		String pathBinding = path + File.separator + bindingName + "_" + previousStep.getStepLabel() +JSON;
		readDataset(pathBinding, bindingName, vtlBindings);
	}
	
	public void readDataset(String pathBindings,String bindingName,VtlBindings vtlBindings) {
		vtlExecute.putVtlDataset(pathBindings, bindingName, vtlBindings);
	}

	public void writeTempBindings(Path inDirectory, String dataMode, VtlBindings vtlBindings, StepEnum step)  {
		Path tempOutputPath = FileUtilsInterface.transformToTemp(inDirectory).resolve(dataMode+"_"+step.getStepLabel()+JSON);
		vtlExecute.writeJsonDataset(dataMode, tempOutputPath, vtlBindings);
	}
	
}
