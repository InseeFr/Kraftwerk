package fr.insee.kraftwerk.core.utils.files;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Interface to use to interact with storage
 */
public interface FileUtilsInterface {
    //Common methods
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

    static Path convertToPath(String userField, Path inputDirectory) throws KraftwerkException {
        if (userField != null && !"null".equals(userField) && !userField.isEmpty()) {
            Path inputPath = inputDirectory.resolve(userField);
            if (!new File(inputPath.toUri()).exists()) {
                throw new KraftwerkException(400, String.format("The input folder \"%s\" does not exist in \"%s\".", userField, inputDirectory));
            }
            return inputPath;
        } else {
            return null;
        }
    }

    static URL convertToUrl(String userField, Path inputDirectory) {
        if (userField == null) {
            return null;
        }
        try {
            if (userField.startsWith("http")) {
                return new URI(userField).toURL();
            }
            return inputDirectory.resolve(userField).toFile().toURI().toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            return null;
        }
    }

    //Methods to implement
    //File system interactions
    /**
     * Read a file
     * @param path path of file
     * @return an InputStream of content of file
     */
    InputStream readFile(String path);
    /**
     * Write string into a file
     * @param path path of the file
     * @param toWrite String to write
     * @param replace true if overwrite, false if append
     */
    void writeFile(String path, String toWrite, boolean replace);
    /**
     * Move file from source to destination (depends on the file system)
     * @param srcPath source path
     * @param dstPath destination path
     * @throws KraftwerkException
     */
    void moveFile(String srcPath, String dstPath) throws KraftwerkException;
    /**
     * Move file from local file system to another path
     * @param fileSystemPath local path
     * @param dstPath destination path
     * @throws KraftwerkException
     */
    void moveFile(Path fileSystemPath, String dstPath) throws KraftwerkException;

    /**
     * Deletes a directory recursively
     * @param directoryPath path of directory
     * @throws KraftwerkException if exception
     */
    void deleteDirectory(Path directoryPath) throws KraftwerkException;
    /**
     * Find the file in the folder of a campaign
     * @param directory directory where the file should be
     * @param fileRegex regex of the file to match
     * @return Path of the file
     * @throws KraftwerkException if no file found
     */
    String findFile(String directory, String fileRegex) throws KraftwerkException;

    //File listing
    /**
     * List file names in a directory
     * @param dir directory to list files from
     * @return a list of file names
     */
    List<String> listFileNames(String dir);

    /**
     * List file paths in a directory
     * @param dir directory to list files from
     * @return a list of file paths
     */
    List<String> listFilePaths(String dir);

    /**
     * Create parent directories of the file
     * @param path path of the file
     */
    void createDirectoryIfNotExist(Path path);

    //Checks
    /**
     * Check if file exists
     * @param path path of file
     * @return true if file exists, false otherwise
     */
    boolean isFileExists(String path);
    /**
     * Checks if path is a file or directory or neither
     * @param path path of file
     * @return true if directory, false if file, null if neither
     */
    @Nullable
    Boolean isDirectory(String path);

    /**
     * Returns the size of a file
     * @param path path of file
     * @return size of file
     */
    long getSizeOf(String path);

    //Misc.
    /**
     * Move the input file to another directory to archive it
     */
    void renameInputFile(Path inDirectory);
    void archiveInputFiles(UserInputsFile userInputsFile) throws KraftwerkException;
    Path getTempVtlFilePath(UserInputs userInputs, String step, String dataset);
}
