package fr.insee.kraftwerk.api.process;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.MetadataUtils;
import fr.insee.kraftwerk.core.sequence.*;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionLog;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Log4j2
public class MainProcessing {

	private final ControlInputSequence controlInputSequence;
	private final boolean fileByFile;
	private final boolean withAllReportingData;
	private final boolean withDDI;


	/* SPECIFIC VARIABLES */
	private final String inDirectoryParam;
	@Getter
	private Path inDirectory;
	
	@Getter
	private UserInputsFile userInputsFile; // if main is called on all files
	List<UserInputsFile> userInputsFileList; // for file by file process
	@Getter
	private VtlBindings vtlBindings = new VtlBindings();
	private KraftwerkExecutionLog kraftwerkExecutionLog;
	private final List<KraftwerkError> errors = new ArrayList<>();
	private LocalDateTime executionDateTime;

	private Path databasePath;
	
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
		//iterate on file(s)
		for (UserInputsFile userFile : userInputsFileList) {
			this.userInputsFile = userFile;
			vtlBindings = new VtlBindings();
			try (Connection readDatabaseConnection = SqlUtils.openConnection()) {
				Statement readDatabase = readDatabaseConnection.createStatement();
				unimodalProcess(readDatabase);
			} catch (SQLException e) {
				log.error(e.toString());
				throw new KraftwerkException(500, "SQL error on reading");
			}
			multimodalProcess();
			try (Connection writeDatabaseConnection = SqlUtils.openConnection(databasePath)) {
				if(writeDatabaseConnection == null){
					throw new KraftwerkException(500, "Error during database creation");
				}
				Statement writeDatabase = writeDatabaseConnection.createStatement();
				outputFileWriter(writeDatabase);
			} catch (SQLException e) {
				log.error(e.toString());
				throw new KraftwerkException(500, "SQL error on writing");
			}
        }
		writeErrors();
		kraftwerkExecutionLog.setEndTimeStamp(System.currentTimeMillis());
		writeLog();
	}

	/* Step 1 : Init */
	public void init() throws KraftwerkException {
		kraftwerkExecutionLog = new KraftwerkExecutionLog(); //Init logger
		this.executionDateTime = LocalDateTime.now();

		inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);

		Path tmpDirectory = fr.insee.kraftwerk.core.utils.FileUtils.transformToTemp(inDirectory);
		this.databasePath = tmpDirectory.resolve(Path.of( executionDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_hhmmssSSS"))+".duckdb"));

		String campaignName = inDirectory.getFileName().toString();
		log.info("Kraftwerk main service started for campaign: " + campaignName);

		userInputsFile = controlInputSequence.getUserInputs(inDirectory);

		metadataModels = withDDI ? MetadataUtils.getMetadata(userInputsFile.getModeInputsMap()) : MetadataUtils.getMetadataFromLunatic(userInputsFile.getModeInputsMap());

		userInputsFileList = getUserInputsFile(userInputsFile, fileByFile);

		// Check size of data files and throw an exception if it is too big .Limit is 400 Mo for one processing (one file or data folder if not file by file).
		//In case of file-by-file processing we check the size of each file.
		if (Boolean.TRUE.equals(fileByFile)) {
			for (UserInputsFile userInputs : userInputsFileList) {
				isDataTooBig(userInputs,"At least one file size is greater than 400Mo. Split data files greater than 400Mo.", limitSize);
			}
		}else{
			//In case of main processing we check the folder
			isDataTooBig(userInputsFile,"Data folder size is greater than 400Mo. Use file-by-file processing.", limitSize);
		}



	}

	/* Step 2 : unimodal data */
	private void unimodalProcess(Statement database) throws KraftwerkException {
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence(withAllReportingData);
		for (String dataMode : userInputsFile.getModeInputsMap().keySet()) {
			MetadataModel metadataForMode = metadataModels.get(dataMode);
			buildBindingsSequence.buildVtlBindings(userInputsFile, dataMode, vtlBindings, metadataForMode, withDDI, kraftwerkExecutionLog, database);
			UnimodalSequence unimodal = new UnimodalSequence();
			unimodal.applyUnimodalSequence(userInputsFile, dataMode, vtlBindings, errors, metadataModels);
		}
	}

	/* Step 3 : multimodal VTL data processing */
	private void multimodalProcess(){
		MultimodalSequence multimodalSequence = new MultimodalSequence();
		multimodalSequence.multimodalProcessing(userInputsFile, vtlBindings, errors, metadataModels);
	}

	/* Step 4 : Write output files */
	private void outputFileWriter(Statement database) throws KraftwerkException {
		WriterSequence writerSequence = new WriterSequence();
		writerSequence.writeOutputFiles(inDirectory, executionDateTime, vtlBindings, userInputsFile.getModeInputsMap(), metadataModels, errors, database);
	}

	/* Step 5 : Write errors */
	private void writeErrors() {
		TextFileWriter.writeErrorsFile(inDirectory, executionDateTime, errors);
	}


	/* Step 6 : Write log */
	private void writeLog() {TextFileWriter.writeLogFile(inDirectory, executionDateTime, kraftwerkExecutionLog);}

	private static List<UserInputsFile> getUserInputsFile(UserInputsFile source, boolean fileByFile) throws KraftwerkException {
		List<UserInputsFile> userInputsFileList = new ArrayList<>();
		if(Boolean.TRUE.equals(fileByFile)){
			for (String dataMode : source.getModeInputsMap().keySet()) {
				List<Path> dataFiles = getFilesToProcess(source, dataMode);
				for (Path dataFile : dataFiles) {
					UserInputsFile currentFileInputs = new UserInputsFile(source.getUserInputFile(), source.getUserInputFile().getParent());
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
		}else{
			userInputsFileList.add(source);
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
