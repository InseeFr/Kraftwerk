package fr.insee.kraftwerk.core.parsers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import lombok.extern.slf4j.Slf4j;

/**
 * Template method for data parsers.
 */
@Slf4j
public abstract class DataParser {

	protected final SurveyRawData data;

	/**
	 * Parser constructor.
	 * @param data The SurveyRawData to be filled by the parseSurveyData method.
	 *             The variables must have been previously set.
	 */
	public DataParser(SurveyRawData data) {
		if (data.getVariablesMap() == null) {
			log.debug("No variables map set on data object. Parsing will most likely fail.");
		}
		this.data = data;
	}

	/**
	 * Fill the data object with the content of the file or folder given.
	 *
	 * @param dataPath A data file, or a folder only containing data files.
	 * @throws NullException 
	 */
	public final void parseSurveyData(Path dataPath) throws NullException {

		if (Files.isRegularFile(dataPath)) {
			parseDataFile(dataPath);
		}

		else if (Files.isDirectory(dataPath)) {
			try (Stream<Path> stream = Files.list(dataPath)){
				stream.forEach(t -> {
					try {
						parseDataFile(t);
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
	 * @throws NullException 
	 */
	abstract void parseDataFile(Path dataFilePath) throws NullException;

}
