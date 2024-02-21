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
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.metadata.MetadataUtils;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.sequence.BuildBindingsSequence;
import fr.insee.kraftwerk.core.sequence.ControlInputSequence;
import fr.insee.kraftwerk.core.sequence.MultimodalSequence;
import fr.insee.kraftwerk.core.sequence.UnimodalSequence;
import fr.insee.kraftwerk.core.sequence.WriterSequence;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionLog;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

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
			for (UserInputsFile userFile : userInputsFileList) {
				this.userInputsFile = userFile;
				vtlBindings = new VtlBindings();
				unimodalProcess();
				multimodalProcess();
				outputFileWriter();
				kraftwerkExecutionLog.getOkFileNames().add(userInputsFile.getUserInputFile().getFileName().toString());
			}
		} else {
			unimodalProcess();
			multimodalProcess();
			outputFileWriter();
			kraftwerkExecutionLog.getOkFileNames().add(userInputsFile.getUserInputFile().getFileName().toString());
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
		if (withDDI) metadataVariables = MetadataUtils.getMetadata(userInputsFile.getModeInputsMap());
		if (!withDDI) metadataVariables = MetadataUtils.getMetadataFromLunatic(userInputsFile.getModeInputsMap());
	
		if (fileByFile) userInputsFileList = getUserInputsFile(userInputsFile);

	}

	/* Step 2 : unimodal data */
	private void unimodalProcess() throws NullException {
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence(withAllReportingData);
		for (String dataMode : userInputsFile.getModeInputsMap().keySet()) {
			buildBindingsSequence.buildVtlBindings(userInputsFile, dataMode, vtlBindings, metadataVariables, withDDI);
			UnimodalSequence unimodal = new UnimodalSequence();
			unimodal.applyUnimodalSequence(userInputsFile, dataMode, vtlBindings, errors, metadataVariables);
		}
	}

	/* Step 3 : multimodal VTL data processing */
	private void multimodalProcess() {
		MultimodalSequence multimodalSequence = new MultimodalSequence();
		multimodalSequence.multimodalProcessing(userInputsFile, vtlBindings, errors, metadataVariables);
	}

	/* Step 4 : Write output files */
	private void outputFileWriter() throws KraftwerkException {
		WriterSequence writerSequence = new WriterSequence();
		writerSequence.writeOutputFiles(inDirectory, vtlBindings, userInputsFile.getModeInputsMap(), metadataVariables, errors, kraftwerkExecutionLog);
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

}
