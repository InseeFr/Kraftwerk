package fr.insee.kraftwerk.api.process;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.MetadataUtils;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.sequence.BuildBindingsSequence;
import fr.insee.kraftwerk.core.sequence.ControlInputSequence;
import fr.insee.kraftwerk.core.sequence.MultimodalSequence;
import fr.insee.kraftwerk.core.sequence.UnimodalSequence;
import fr.insee.kraftwerk.core.sequence.WriterSequence;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

@Log4j2
public class MainProcessing {

	private ControlInputSequence controlInputSequence;
	private boolean fileByFile;
	private boolean withAllReportingData;
	private boolean withDDI;


	/* SPECIFIC VARIABLES */
	private String inDirectoryParam;
	@Getter
	private Path inDirectory;
	
	@Getter
	private UserInputs userInputs; // if main is called on all files
	List<UserInputs> userInputsList; // for file by file process
	@Getter
	private VtlBindings vtlBindings = new VtlBindings();
	private List<KraftwerkError> errors = new ArrayList<>();
	@Getter
	private Map<String, VariablesMap> metadataVariables;

	public MainProcessing(String inDirectoryParam, boolean fileByFile,boolean withAllReportingData,boolean withDDI, String defaultDirectory) {
		super();
		this.inDirectoryParam = inDirectoryParam;
		this.fileByFile = fileByFile;
		this.withAllReportingData = withAllReportingData;
		this.withDDI=withDDI;
		controlInputSequence = new ControlInputSequence(defaultDirectory);
	}
	
	public MainProcessing(String inDirectoryParam, boolean fileByFile, String defaultDirectory) {
		super();
		this.inDirectoryParam = inDirectoryParam;
		this.fileByFile = fileByFile;
		this.withAllReportingData = !fileByFile;
		this.withDDI=true;
		controlInputSequence = new ControlInputSequence(defaultDirectory);
	}


	public void runMain() throws KraftwerkException {
		init();
		if (Boolean.TRUE.equals(fileByFile)) { //iterate on files
			for (UserInputs userInputsFile : userInputsList) {
				userInputs = userInputsFile;
				vtlBindings = new VtlBindings();
				unimodalProcess();
				multimodalProcess();
				outputFileWriter();
			}
		} else {
			unimodalProcess();
			multimodalProcess();
			outputFileWriter();
		}
		writeErrors();
	}

	/* Step 1 : Init */
	public void init() throws KraftwerkException {
		inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);

		String campaignName = inDirectory.getFileName().toString();
		log.info("Kraftwerk main service started for campaign: " + campaignName);

		userInputs = controlInputSequence.getUserInputs(inDirectory);
		if (withDDI) metadataVariables = MetadataUtils.getMetadata(userInputs.getModeInputsMap());
		if (!withDDI) metadataVariables = MetadataUtils.getMetadataFromLunatic(userInputs.getModeInputsMap());
	
		if (fileByFile) userInputsList = getUserInputs(userInputs);

		// Check size of data files and throw an exception if it is too big .Limit is 400 Mo for one processing (one file or data folder if not file by file).
		//In case of file-by-file processing we check the size of each file.
		long limitSize = 419430400L;
		if (fileByFile) {
			for (UserInputs userInputsFile : userInputsList) {
				isDataTooBig(userInputsFile,"At least one file size is greater than 400Mo. Split data files greater than 400 MB.", limitSize);
			}
		}
		//In case of main processing we check the folder
		if (!fileByFile) {
			isDataTooBig(userInputs,"Data folder size is greater than 400Mo. Use file-by-file processing.", limitSize);
		}

	}

	/* Step 2 : unimodal data */
	private void unimodalProcess() throws KraftwerkException {
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence(withAllReportingData);
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			buildBindingsSequence.buildVtlBindings(userInputs, dataMode, vtlBindings, metadataVariables, withDDI);
			UnimodalSequence unimodal = new UnimodalSequence();
			unimodal.applyUnimodalSequence(userInputs, dataMode, vtlBindings, errors, metadataVariables);
		}
	}

	/* Step 3 : multimodal VTL data processing */
	private void multimodalProcess() {
		MultimodalSequence multimodalSequence = new MultimodalSequence();
		multimodalSequence.multimodalProcessing(userInputs, vtlBindings, errors, metadataVariables);
	}

	/* Step 4 : Write output files */
	private void outputFileWriter() throws KraftwerkException {
		WriterSequence writerSequence = new WriterSequence();
		writerSequence.writeOutputFiles(inDirectory, vtlBindings, userInputs.getModeInputsMap(),
				userInputs.getMultimodeDatasetName(), metadataVariables, errors);
	}

	/* Step 5 : Write errors */
	private void writeErrors() {
		TextFileWriter.writeErrorsFile(inDirectory, errors);
	}

	private static List<UserInputs> getUserInputs(UserInputs source) throws KraftwerkException {
		List<UserInputs> userInputsList = new ArrayList<>();
		for (String dataMode : source.getModeInputsMap().keySet()) {
			List<Path> dataFiles = getFilesToProcess(source, dataMode);
			for (Path dataFile : dataFiles) {
				UserInputs currentFileInputs = new UserInputs(source.getUserInputFile(),source.getUserInputFile().getParent());
				currentFileInputs.setVtlReconciliationFile(source.getVtlReconciliationFile());
				currentFileInputs.setVtlInformationLevelsFile(source.getVtlInformationLevelsFile());
				currentFileInputs.setVtlTransformationsFile(source.getVtlTransformationsFile());
				currentFileInputs.setMultimodeDatasetName(source.getMultimodeDatasetName());
				ModeInputs sourceModeInputs = source.getModeInputs(dataMode);
				ModeInputs currentFileModeInputs = new ModeInputs();
				currentFileModeInputs.setDataFile(dataFile);
				currentFileModeInputs.setDdiUrl(sourceModeInputs.getDdiUrl());
				currentFileModeInputs.setLunaticFile(sourceModeInputs.getLunaticFile());
				currentFileModeInputs.setDataFormat(sourceModeInputs.getDataFormat().toString());
				currentFileModeInputs.setDataMode(sourceModeInputs.getDataMode());
				currentFileModeInputs.setModeVtlFile(sourceModeInputs.getModeVtlFile());
				currentFileModeInputs.setParadataFolder(sourceModeInputs.getParadataFolder());
				currentFileModeInputs.setReportingDataFile(sourceModeInputs.getReportingDataFile());
				currentFileInputs.getModeInputsMap().put(dataMode, currentFileModeInputs);
				userInputsList.add(currentFileInputs);
			}
		}
		return userInputsList;
	}

	private static List<Path> getFilesToProcess(UserInputs userInputs, String dataMode) {
		List<Path> files = new ArrayList<>();
		ModeInputs modeInputs = userInputs.getModeInputs(dataMode);
		Path dataPath = modeInputs.getDataFile();
		if (dataPath == null)
			log.error("Datapath is null");
		else {
			if (Files.isRegularFile(dataPath)) {
				files.add(dataPath);
			} else if (Files.isDirectory(dataPath)) {
				try (Stream<Path> stream = Files.list(dataPath)) {
					stream.forEach(files::add);
				} catch (IOException e) {
					log.error(String.format("IOException occurred when trying to list data files of folder: %s",
							dataPath));
				}
			} else {
				log.warn(String.format("Data path given could not be identified as a file or folder: %s", dataPath));
			}
		}
		return files;
	}

	private void isDataTooBig(UserInputs userInputsFile, String errorMessage, long limitSize) throws KraftwerkException {
		for (String dataMode : userInputs.getModeInputsMap().keySet()){
			long dataSize = FileUtils.sizeOf(userInputsFile.getModeInputs(dataMode).getDataFile().toFile());
			if (dataSize > limitSize) {
				log.error("Size of data folder/file {} : {}",userInputsFile.getModeInputs(dataMode).getDataFile(), FileUtils.byteCountToDisplaySize(dataSize));
				throw new KraftwerkException(413,errorMessage);
			}
		}
	}

}
