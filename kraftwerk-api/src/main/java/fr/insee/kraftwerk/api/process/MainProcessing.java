package fr.insee.kraftwerk.api.process;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.metadata.MetadataUtils;
import fr.insee.kraftwerk.core.sequence.BuildBindingsSequence;
import fr.insee.kraftwerk.core.sequence.ControlInputSequence;
import fr.insee.kraftwerk.core.sequence.InsertDatabaseSequence;
import fr.insee.kraftwerk.core.sequence.MultimodalSequence;
import fr.insee.kraftwerk.core.sequence.UnimodalSequence;
import fr.insee.kraftwerk.core.sequence.WriterSequence;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	private KraftwerkExecutionContext kraftwerkExecutionContext;
	private final FileUtilsInterface fileUtilsInterface;

	
	/**
	 * Map by mode
	 */
	@Getter
	private Map<String, MetadataModel> metadataModels;

	private final long limitSize;

	public MainProcessing(String inDirectoryParam, boolean fileByFile,boolean withAllReportingData,boolean withDDI, String defaultDirectory, long limitSize, FileUtilsInterface fileUtilsInterface) {
		super();
		this.inDirectoryParam = inDirectoryParam;
		this.fileByFile = fileByFile;
		this.withAllReportingData = withAllReportingData;
		this.withDDI=withDDI;
		this.limitSize = limitSize;
		controlInputSequence = new ControlInputSequence(defaultDirectory, fileUtilsInterface);
		this.fileUtilsInterface = fileUtilsInterface;
	}
	
	public MainProcessing(String inDirectoryParam, boolean fileByFile, String defaultDirectory, long limitSize, FileUtilsInterface fileUtilsInterface) {
		super();
		this.inDirectoryParam = inDirectoryParam;
		this.fileByFile = fileByFile;
		this.withAllReportingData = !fileByFile;
		this.withDDI=true;
		this.limitSize = limitSize;
		controlInputSequence = new ControlInputSequence(defaultDirectory, fileUtilsInterface);
		this.fileUtilsInterface = fileUtilsInterface;
	}


	public void runMain() throws KraftwerkException {
		init();
		//iterate on file(s)
		try (Connection writeDatabaseConnection = SqlUtils.openConnection()) {
			for (UserInputsFile userFile : userInputsFileList) {
				this.userInputsFile = userFile;
				vtlBindings = new VtlBindings();
				unimodalProcess();
				multimodalProcess();
				try(Statement writeDatabase = writeDatabaseConnection.createStatement()){
					insertDatabase(writeDatabase);
				}
			}
			//Export from database
			try(Statement writeDatabase = writeDatabaseConnection.createStatement()){
				outputFileWriter(writeDatabase);
			}
			writeErrors();
			kraftwerkExecutionContext.setEndTimeStamp(System.currentTimeMillis());
			writeLog();
		} catch (SQLException e) {
			log.error(e.toString());
			throw new KraftwerkException(500, "SQL Error");
		}
	}

	/* Step 1 : Init */
	public void init() throws KraftwerkException {
		kraftwerkExecutionContext = new KraftwerkExecutionContext(); //Init logger

		inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);

		String campaignName = inDirectory.getFileName().toString();
		log.info("Kraftwerk main service started for campaign: {}", campaignName);

		userInputsFile = controlInputSequence.getUserInputs(inDirectory, fileUtilsInterface);

		metadataModels = withDDI ? MetadataUtils.getMetadata(userInputsFile.getModeInputsMap(), fileUtilsInterface) : MetadataUtils.getMetadataFromLunatic(userInputsFile.getModeInputsMap(), fileUtilsInterface);

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
	private void unimodalProcess() throws KraftwerkException {
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence(withAllReportingData, fileUtilsInterface);
		for (String dataMode : userInputsFile.getModeInputsMap().keySet()) {
			MetadataModel metadataForMode = metadataModels.get(dataMode);
			buildBindingsSequence.buildVtlBindings(userInputsFile, dataMode, vtlBindings, metadataForMode, withDDI, kraftwerkExecutionContext);
			UnimodalSequence unimodal = new UnimodalSequence();
			unimodal.applyUnimodalSequence(userInputsFile, dataMode, vtlBindings, kraftwerkExecutionContext,
					metadataModels,
					fileUtilsInterface);
		}
	}

	/* Step 3 : multimodal VTL data processing */
	private void multimodalProcess(){
		MultimodalSequence multimodalSequence = new MultimodalSequence();
		multimodalSequence.multimodalProcessing(userInputsFile, vtlBindings, kraftwerkExecutionContext, metadataModels, fileUtilsInterface);
	}

	/* Step 4 : Insert into SQL database */
	private void insertDatabase(Statement database) {
		InsertDatabaseSequence insertDatabaseSequence = new InsertDatabaseSequence();
		insertDatabaseSequence.insertDatabaseProcessing(vtlBindings, database);
	}

	/* Step 5 : Write output files */
	private void outputFileWriter(Statement database) throws KraftwerkException {
		WriterSequence writerSequence = new WriterSequence();
		writerSequence.writeOutputFiles(inDirectory, vtlBindings, userInputsFile.getModeInputsMap(), metadataModels, kraftwerkExecutionContext, database, fileUtilsInterface);
	}

	/* Step 5 : Write errors */
	private void writeErrors() {
		TextFileWriter.writeErrorsFile(inDirectory, kraftwerkExecutionContext, fileUtilsInterface);
	}


	/* Step 6 : Write log */
	private void writeLog() {TextFileWriter.writeLogFile(inDirectory, kraftwerkExecutionContext, fileUtilsInterface);}

	private static List<UserInputsFile> getUserInputsFile(UserInputsFile source, boolean fileByFile) throws KraftwerkException {
		List<UserInputsFile> userInputsFileList = new ArrayList<>();
		if(Boolean.TRUE.equals(fileByFile)){
			for (String dataMode : source.getModeInputsMap().keySet()) {
				List<Path> dataFiles = getFilesToProcess(source, dataMode);
				for (Path dataFile : dataFiles) {
					UserInputsFile currentFileInputs = new UserInputsFile(source.getUserInputFile(), source.getUserInputFile().getParent(), source.getFileUtilsInterface());
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
		if(userInputsFile.getFileUtilsInterface().isDirectory(dataPath.toString()) == null){
			log.warn("Data path given could not be identified as a file or folder: {}", dataPath);
			return files;
		}
		if(Boolean.TRUE.equals(userInputsFile.getFileUtilsInterface().isDirectory(dataPath.toString()))){
			for(String path : userInputsFile.getFileUtilsInterface().listFilePaths(dataPath.toString())){
				files.add(Path.of(path));
			}
		}else{
			files.add(dataPath);
		}
		return files;
	}

	private void isDataTooBig(UserInputsFile userInputsFile, String errorMessage, long limitSize) throws KraftwerkException {
		for (String dataMode : userInputsFile.getModeInputsMap().keySet()){
			long dataSize = userInputsFile.getFileUtilsInterface().getSizeOf(userInputsFile.getModeInputs(dataMode).getDataFile().toString());
			if (dataSize > limitSize) {
				log.error("Size of data folder/file {} : {}",userInputsFile.getModeInputs(dataMode).getDataFile(), dataSize);
				throw new KraftwerkException(413,errorMessage);
			}
		}
	}

}
