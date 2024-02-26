package fr.insee.kraftwerk.core.parsers;

import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionLog;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Template method for data parsers.
 */
@Log4j2
public abstract class DataParser {

	public static final String DATAPATH_IS_NULL = "Datapath is null";
	protected final SurveyRawData data;

	/**
	 * Parser constructor.
	 * @param data The SurveyRawData to be filled by the parseSurveyData method.
	 *             The variables must have been previously set.
	 */
	protected DataParser(SurveyRawData data) {
		if (data.getMetadataModel() == null || data.getMetadataModel().getVariables() == null) {
			log.debug("No metadata set on data object. Parsing will most likely fail.");
		}
		this.data = data;
	}

	/**
	 * Fill the data object with the content of the file or folder given.
	 *
	 * @param dataPath A data file, or a folder only containing data files.
	 * @throws NullException -- throws null exception if datapath or a file is missing
	 */
	public final void parseSurveyData(Path dataPath, KraftwerkExecutionLog kraftwerkExecutionLog) throws NullException {
		if (dataPath == null){
			log.error(DATAPATH_IS_NULL);
			throw new NullException(DATAPATH_IS_NULL);
		}
		if (Files.isRegularFile(dataPath)) {
			parseDataFile(dataPath);
			if(kraftwerkExecutionLog != null) {
				kraftwerkExecutionLog.getOkFileNames().add(dataPath.getFileName().toString());
			}
		}
		else if (Files.isDirectory(dataPath)) {
			try (Stream<Path> stream = Files.list(dataPath)){
				stream.forEach(t -> {
					try {
						parseDataFile(t);
						if(kraftwerkExecutionLog != null) {
							kraftwerkExecutionLog.getOkFileNames().add(t.getFileName().toString());
						}
					} catch (NullException e) {
						log.error("IOException occurred when trying to list data file: {} in folder {}", t, dataPath);
					}
				});
			} catch (IOException e) {
				log.error(String.format("IOException occurred when trying to list data files of folder: %s", dataPath));
			}
		}

		else {
			log.warn(String.format("Data path given could not be identified as a file or folder: %s", dataPath));
			log.warn("No data was parsed.");
		}

	}

	/**
	 * Fill the data object with the content of the file or folder given.
	 *
	 * @param dataPath A data file, or a folder only containing data files.
	 * @throws NullException -- throws null exception if datapath or a file is missing
	 */
	public final void parseSurveyDataWithoutDDI(Path dataPath, Path lunaticFile, KraftwerkExecutionLog kraftwerkExecutionLog) throws NullException {
		if (dataPath == null){
			log.error(DATAPATH_IS_NULL);
			throw new NullException(DATAPATH_IS_NULL);
		}
		if (Files.isRegularFile(dataPath)) {
			parseDataFileWithoutDDI(dataPath,lunaticFile);
			if(kraftwerkExecutionLog != null) {
				kraftwerkExecutionLog.getOkFileNames().add(dataPath.getFileName().toString());
			}
		}

		else if (Files.isDirectory(dataPath)) {
			try (Stream<Path> stream = Files.list(dataPath)){
				stream.forEach(t -> {
					try {
						parseDataFileWithoutDDI(t,lunaticFile);
						if(kraftwerkExecutionLog != null) {
							kraftwerkExecutionLog.getOkFileNames().add(t.getFileName().toString());
						}
					} catch (NullException e) {
						log.error("IOException occurred when trying to list data file: {} in folder {}", t, dataPath);
					}
				});
			} catch (IOException e) {
				log.error(String.format("IOException occurred when trying to list data files of folder: %s", dataPath));
			}
		}

		else {
			log.warn(String.format("Data path given could not be identified as a file or folder: %s", dataPath));
			log.warn("No data was parsed.");
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
