package fr.insee.kraftwerk.core.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionLog;
import lombok.extern.log4j.Log4j2;


/**
 * Utils class to write text files.
 */
@Log4j2
public class TextFileWriter {
	
	private TextFileWriter() {
		//Utility class
	}

    /**
     * Write a text file.
     * @param filePath Path to the file.
     * @param fileContent Content of the text file.
     */
    public static void writeFile(Path filePath, String fileContent, FileUtilsInterface fileUtilsInterface){
		fileUtilsInterface.writeFile(String.valueOf(filePath), fileContent, true);
		log.info(String.format("Text file: %s successfully written", filePath));
	}
    
	public static void writeErrorsFile(Path inDirectory, LocalDateTime localDateTime, List<KraftwerkError> errors, FileUtilsInterface fileUtilsInterface) {
		Path tempOutputPath = FileUtilsInterface.transformToOut(inDirectory,localDateTime)
				.resolve(Constants.ERRORS_FILE_NAME);
		FileSystemImpl.createDirectoryIfNotExist(tempOutputPath.getParent());

		//Write errors file
		if (!errors.isEmpty()) {
			for (KraftwerkError error : errors) {
				fileUtilsInterface.writeFile(tempOutputPath.toString(), error.toString(), false);
			}
			log.info(String.format("Text file: %s successfully written", tempOutputPath));
		} else {
			log.debug("No error found during VTL transformations");
		}
	}

	public static void writeLogFile(Path inDirectory, LocalDateTime localDateTime, KraftwerkExecutionLog kraftwerkExecutionLog, FileUtilsInterface fileUtilsInterface){
		Path tempOutputPath = FileUtilsInterface.transformToOut(inDirectory,localDateTime);
		tempOutputPath = tempOutputPath.resolve(inDirectory.getFileName() + "_LOG_" + kraftwerkExecutionLog.getStartTimeStamp() +".txt");

		fileUtilsInterface.writeFile(tempOutputPath.toString(), kraftwerkExecutionLog.getFormattedString(), false);
	}
}
