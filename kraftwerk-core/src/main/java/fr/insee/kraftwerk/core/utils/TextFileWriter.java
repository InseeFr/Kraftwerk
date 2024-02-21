package fr.insee.kraftwerk.core.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import fr.insee.kraftwerk.core.KraftwerkError;
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
    public static void writeFile(Path filePath, String fileContent){
        try (FileWriter myWriter = new FileWriter(filePath.toFile())){
            myWriter.write(fileContent);
            log.info(String.format("Text file: %s successfully written", filePath));
        } catch (IOException e) {
            log.warn(String.format("Error occurred when trying to write text file: %s", filePath), e);
        }
    }
    
	public static void writeErrorsFile(Path inDirectory, List<KraftwerkError> errors) {
		Path tempOutputPath = FileUtils.transformToOut(inDirectory).resolve("errors.txt");
		FileUtils.createDirectoryIfNotExist(tempOutputPath.getParent());

		//Write errors file
		if (!errors.isEmpty()) {
			try (FileWriter myWriter = new FileWriter(tempOutputPath.toFile(),true)){
				for (KraftwerkError error : errors){
					myWriter.write(error.toString());
				}
				log.info(String.format("Text file: %s successfully written", tempOutputPath));
			} catch (IOException e) {
				log.warn(String.format("Error occurred when trying to write text file: %s", tempOutputPath), e);
			}
		} else {
			log.debug("No error found during VTL transformations");
		}
	}

	public static void writeLogFile(Path inDirectory, KraftwerkExecutionLog kraftwerkExecutionLog){
		Path tempOutputPath = FileUtils.transformToOut(inDirectory).resolve(inDirectory.getFileName() + "_LOG_" + kraftwerkExecutionLog.getStartTimeStamp() +".txt");
		FileUtils.createDirectoryIfNotExist(tempOutputPath.getParent());

		try (FileWriter myWriter = new FileWriter(tempOutputPath.toFile(),true)){
			myWriter.write(kraftwerkExecutionLog.getFormattedString());
		}catch (IOException e) {
			log.warn(String.format("Error occurred when trying to write log file: %s", tempOutputPath), e);
		}
	}
}
