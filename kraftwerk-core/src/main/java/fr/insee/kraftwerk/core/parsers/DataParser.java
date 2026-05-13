package fr.insee.kraftwerk.core.parsers;

import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;

/**
 * Template method for data parsers.
 */
@Log4j2
public abstract class DataParser {
	public static final String DATAPATH_IS_NULL = "Datapath is null";
	protected final SurveyRawData data;
	protected final FileUtilsInterface fileUtilsInterface;

	/**
	 * Parser constructor.
	 * @param data The SurveyRawData to be filled by the parseSurveyData method.
	 *             The variables must have been previously set.
	 */
	protected DataParser(SurveyRawData data, FileUtilsInterface fileUtilsInterface) {
		if (data.getMetadataModel() == null || data.getMetadataModel().getVariables() == null) {
			log.debug("No metadata set on data object. Parsing will most likely fail.");
		}
		this.data = data;
		this.fileUtilsInterface = fileUtilsInterface;
	}

	/**
	 * Fill the data object with the content of the file or folder given.
	 *
	 * @param dataPath A data file, or a folder only containing data files.
	 * @throws NullException -- throws null exception if datapath or a file is missing
	 */
	public final void parseSurveyData(Path dataPath, KraftwerkExecutionContext kraftwerkExecutionContext) throws NullException {
		checkDatapathIsNotNull(dataPath);
		if(fileUtilsInterface.isDirectory(dataPath.toString()) == null){
			log.warn("Data path given could not be identified as a file or folder: {} /n No data was parsed.", dataPath);
		}
		if(Boolean.FALSE.equals(fileUtilsInterface.isDirectory(dataPath.toString()))){
			parseDataFile(dataPath);
			if(kraftwerkExecutionContext != null) {
				kraftwerkExecutionContext.getOkFileNames().add(dataPath.getFileName().toString());
			}
		}
		if(Boolean.TRUE.equals(fileUtilsInterface.isDirectory(dataPath.toString()))){
			for(String path : fileUtilsInterface.listFilePaths(String.valueOf(dataPath))){
				try {
					Path filePath = Path.of(path);
					parseDataFile(filePath);
					if(kraftwerkExecutionContext != null) {
						kraftwerkExecutionContext.getOkFileNames().add(filePath.getFileName().toString());
					}
				} catch (NullException e) {
					log.error("IOException occurred when trying to list data file: {} in folder {}", path, dataPath);
				}
			}
		}
	}

	private static void checkDatapathIsNotNull(Path dataPath) throws NullException {
		if (dataPath == null){
			log.error(DATAPATH_IS_NULL);
			throw new NullException(DATAPATH_IS_NULL);
		}
	}

	/**
	 * Fill the data object with the content of the file or folder given.
	 *
	 * @param dataPath A data file, or a folder only containing data files.
	 * @throws NullException -- throws null exception if datapath or a file is missing
	 */
	public final void parseSurveyDataWithoutDDI(Path dataPath, Path lunaticFile, KraftwerkExecutionContext kraftwerkExecutionContext) throws NullException {
		checkDatapathIsNotNull(dataPath);
		if(fileUtilsInterface.isDirectory(dataPath.toString()) == null){
			log.warn(String.format("Data path given could not be identified as a file or folder: %s /n No data was parsed.", dataPath));
		}
		if(Boolean.FALSE.equals(fileUtilsInterface.isDirectory(dataPath.toString()))) {
			parseDataFileWithoutDDI(dataPath,lunaticFile);
			if(kraftwerkExecutionContext != null) {
				kraftwerkExecutionContext.getOkFileNames().add(dataPath.getFileName().toString());
			}
		}
		if(Boolean.TRUE.equals(fileUtilsInterface.isDirectory(dataPath.toString()))) {
			for(String path : fileUtilsInterface.listFilePaths(dataPath.toString())){
				try {
					Path filePath = Path.of(path);
					parseDataFileWithoutDDI(filePath,lunaticFile);
					if(kraftwerkExecutionContext != null) {
						kraftwerkExecutionContext.getOkFileNames().add(filePath.getFileName().toString());
					}
				} catch (NullException e) {
					log.error("IOException occurred when trying to list data file without DDI: {} in folder {}", path, dataPath);
				}
			}
		}
	}

	/**
	 * Fill the data object with the content of the given file.
	 * @param dataFilePath Path to a data file.
	 * @throws NullException  -- throws null exception if datafilepath is missing
	 */
	abstract void parseDataFile(Path dataFilePath) throws NullException;

	/**
	 * Fill the data object with the content of the given file for treatment without DDI specification
	 * @param dataFilePath Path to a data file.
	 * @throws NullException -- throws null exception if datafilepath is missing
	 */
	void parseDataFileWithoutDDI(Path dataFilePath,Path lunaticFile) throws NullException {
		log.info("Parsing without DDI not implemented for this data format {} {}", dataFilePath, lunaticFile);
	}

}
