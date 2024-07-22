package fr.insee.kraftwerk.core.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		// if we write in temp folder we log in debug mode
		if (pathContainsFolder(filePath,"temp")){
			log.debug(String.format("File: %s successfully written", filePath));
		} else {
			log.info(String.format("File: %s successfully written", filePath));
		}
	}
    
	public static void writeErrorsFile(Path inDirectory, LocalDateTime localDateTime, List<KraftwerkError> errors, FileUtilsInterface fileUtilsInterface) {
		Path tempOutputPath = FileUtilsInterface.transformToOut(inDirectory,localDateTime)
				.resolve(Constants.ERRORS_FILE_NAME);
		fileUtilsInterface.createDirectoryIfNotExist(tempOutputPath.getParent());

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

	public static boolean pathContainsFolder(Path pathString, String folderToFind) {
		// Iterate through the elements of the path
		for (Path element : pathString) {
			if (element.toString().equals(folderToFind)) {
				return true;
			}
		}
		return false;
	}
}
