package fr.insee.kraftwerk.api.process;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.MetadataUtils;
import fr.insee.kraftwerk.core.sequence.*;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionLog;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
	private UserInputsFile userInputsFile; // if main is called on all files
	List<UserInputsFile> userInputsFileList; // for file by file process
	@Getter
	private VtlBindings vtlBindings = new VtlBindings();
	private KraftwerkExecutionLog kraftwerkExecutionLog;
	private List<KraftwerkError> errors = new ArrayList<>();
	
	/**
	 * Map by mode
	 */
	@Getter
	private Map<String, MetadataModel> metadataModels;

	private final long limitSize;

	public MainProcessing(String inDirectoryParam, boolean fileByFile,boolean withAllReportingData,boolean withDDI, String defaultDirectory, long limitSize) {
		super();
		this.inDirectoryParam = inDirectoryParam;
		this.fileByFile = fileByFile;
		this.withAllReportingData = withAllReportingData;
		this.withDDI=withDDI;
		this.limitSize = limitSize;
		controlInputSequence = new ControlInputSequence(defaultDirectory);
	}
	
	public MainProcessing(String inDirectoryParam, boolean fileByFile, String defaultDirectory, long limitSize) {
		super();
		this.inDirectoryParam = inDirectoryParam;
		this.fileByFile = fileByFile;
		this.withAllReportingData = !fileByFile;
		this.withDDI=true;
		this.limitSize = limitSize;
		controlInputSequence = new ControlInputSequence(defaultDirectory);
	}


	public void runMain() throws KraftwerkException {
		init();
		if (Boolean.TRUE.equals(fileByFile)) { //iterate on files
			for (UserInputsFile userFile : userInputsFileList) {
				this.userInputsFile = userFile;
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
		kraftwerkExecutionLog.setEndTimeStamp(System.currentTimeMillis());
		writeLog();
	}

	/* Step 1 : Init */
	public void init() throws KraftwerkException {
		kraftwerkExecutionLog = new KraftwerkExecutionLog(); //Init logger
		inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);

		String campaignName = inDirectory.getFileName().toString();
		log.info("Kraftwerk main service started for campaign: " + campaignName);

		userInputsFile = controlInputSequence.getUserInputs(inDirectory);
		if (withDDI) metadataModels = MetadataUtils.getMetadata(userInputsFile.getModeInputsMap());
		if (!withDDI) metadataModels = MetadataUtils.getMetadataFromLunatic(userInputsFile.getModeInputsMap());

		if (fileByFile) userInputsFileList = getUserInputsFile(userInputsFile);

		// Check size of data files and throw an exception if it is too big .Limit is 400 Mo for one processing (one file or data folder if not file by file).
		//In case of file-by-file processing we check the size of each file.
		if (fileByFile) {
			for (UserInputsFile userInputs : userInputsFileList) {
				isDataTooBig(userInputs,"At least one file size is greater than 400Mo. Split data files greater than 400Mo.", limitSize);
			}
		}
		//In case of main processing we check the folder
		if (!fileByFile) {
			isDataTooBig(userInputsFile,"Data folder size is greater than 400Mo. Use file-by-file processing.", limitSize);
		}

	}

	/* Step 2 : unimodal data */
	private void unimodalProcess() throws KraftwerkException {
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence(withAllReportingData);
		for (String dataMode : userInputsFile.getModeInputsMap().keySet()) {
			MetadataModel metadataForMode = metadataModels.get(dataMode);
			buildBindingsSequence.buildVtlBindings(userInputsFile, dataMode, vtlBindings, metadataForMode, withDDI,kraftwerkExecutionLog);
			UnimodalSequence unimodal = new UnimodalSequence();
			unimodal.applyUnimodalSequence(userInputsFile, dataMode, vtlBindings, errors, metadataModels);
		}
	}

	/* Step 3 : multimodal VTL data processing */
	private void multimodalProcess() {
		MultimodalSequence multimodalSequence = new MultimodalSequence();
		multimodalSequence.multimodalProcessing(userInputsFile, vtlBindings, errors, metadataModels);
	}

	/* Step 4 : Write output files */
	private void outputFileWriter() throws KraftwerkException {
		WriterSequence writerSequence = new WriterSequence();
		writerSequence.writeOutputFiles(inDirectory, vtlBindings, userInputsFile.getModeInputsMap(), metadataModels, errors, kraftwerkExecutionLog);
	}

	/* Step 5 : Write errors */
	private void writeErrors() {
		TextFileWriter.writeErrorsFile(inDirectory, errors);
	}


	/* Step 6 : Write log */
	private void writeLog() {TextFileWriter.writeLogFile(inDirectory,kraftwerkExecutionLog);}

	private static List<UserInputsFile> getUserInputsFile(UserInputsFile source) throws KraftwerkException {
		List<UserInputsFile> userInputsFileList = new ArrayList<>();
		for (String dataMode : source.getModeInputsMap().keySet()) {
			List<Path> dataFiles = getFilesToProcess(source, dataMode);
			for (Path dataFile : dataFiles) {
				UserInputsFile currentFileInputs = new UserInputsFile(source.getUserInputFile(),source.getUserInputFile().getParent());
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
				userInputsFileList.add(currentFileInputs);
			}
		}
		return userInputsFileList;
	}

	private static List<Path> getFilesToProcess(UserInputsFile userInputsFile, String dataMode) {
		List<Path> files = new ArrayList<>();
		ModeInputs modeInputs = userInputsFile.getModeInputs(dataMode);
		Path dataPath = modeInputs.getDataFile();
		if (dataPath == null){
			log.error("Datapath is null");
			return files;
		}
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
		return files;
	}

	private void isDataTooBig(UserInputsFile userInputsFile, String errorMessage, long limitSize) throws KraftwerkException {
		for (String dataMode : userInputsFile.getModeInputsMap().keySet()){
			long dataSize = FileUtils.sizeOf(userInputsFile.getModeInputs(dataMode).getDataFile().toFile());
			if (dataSize > limitSize) {
				log.error("Size of data folder/file {} : {}",userInputsFile.getModeInputs(dataMode).getDataFile(), FileUtils.byteCountToDisplaySize(dataSize));
				throw new KraftwerkException(413,errorMessage);
			}
		}
	}

}
