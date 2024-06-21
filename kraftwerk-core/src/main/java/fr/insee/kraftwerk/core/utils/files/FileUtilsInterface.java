package fr.insee.kraftwerk.core.utils.files;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public interface FileUtilsInterface {
    //TODO organize this
    static Path transformToOut(Path inDirectory) {
        return transformToOther(inDirectory, "out");
    }

    static Path transformToOut(Path inDirectory, LocalDateTime localDateTime) {
        return transformToOther(inDirectory, "out").resolve(localDateTime.format(DateTimeFormatter.ofPattern(Constants.OUTPUT_FOLDER_DATETIME_PATTERN)));
    }

    /**
     * Change /some/path/in/campaign-name to /some/path/temp/campaign-name
     */
    static Path transformToTemp(Path inDirectory) {
        return transformToOther(inDirectory, "temp");
    }

    /**
     * Change /some/path/in/campaign-name to /some/path/__other__/campaign-name
     */
    private static Path transformToOther(Path inDirectory, String other) {
        return "in".equals(inDirectory.getFileName().toString()) ? inDirectory.getParent().resolve(other)
                : transformToOther(inDirectory.getParent(), other).resolve(inDirectory.getFileName());
    }

    void renameInputFile(Path inDirectory);

    void archiveInputFiles(UserInputsFile userInputsFile) throws KraftwerkException;

    void deleteDirectory(Path directoryPath) throws KraftwerkException;

    //TODO change to listFilesNames
    List<String> listFiles(String dir);

    List<String> listFilePaths(String dir);

    Path getTempVtlFilePath(UserInputs userInputs, String step, String dataset);

    Path convertToPath(String userField, Path inputDirectory) throws KraftwerkException;

    URL convertToUrl(String userField, Path inputDirectory);

    /**
     *
     * @param path path of file
     * @return true if directory, false if file, null if neither
     */
    @Nullable
    Boolean isDirectory(String path);

    /**
     *
     * @param path path of file
     * @return size of file
     */
    long getSizeOf(String path);

    /**
     * Write a file in the path
     * @param path path of the file
     * @param toWrite String to write
     * @param replace true if overwrite, false if append
     */
    void writeFile(String path, String toWrite, boolean replace);

    /**
     * Find the file in the folder of a campaign
     * @param directory directory where the file should be
     * @param fileRegex regex of the file to match
     * @return Path of the DDI file
     * @throws KraftwerkException if no file found
     */
    String findFile(String directory, String fileRegex) throws KraftwerkException;
    InputStream readFile(String path);

    boolean isFileExists(String path);
}
