package fr.insee.kraftwerk.core.utils;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Log4j2
public class FileUtils {

	private FileUtils(){
		throw new IllegalStateException("Utility class");
	}

	private static final String ARCHIVE = "Archive";
	
	/**
	 * Change /some/path/in/campaign-name to /some/path/out/campaign-name
	 */
	public static Path transformToOut(Path inDirectory) {
		return transformToOther(inDirectory, "out");
	}
	
	/**
	 * Change /some/path/in/campaign-name to /some/path/temp/campaign-name
	 */
	public static Path transformToTemp(Path inDirectory) {
		return transformToOther(inDirectory, "temp");
	}

	/**
	 * Change /some/path/in/campaign-name to /some/path/__other__/campaign-name
	 */
	private static Path transformToOther(Path inDirectory, String other) {
		return "in".equals(inDirectory.getFileName().toString()) ? inDirectory.getParent().resolve(other)
				: transformToOther(inDirectory.getParent(),other).resolve(inDirectory.getFileName());
	}
	

	
	/**
	 * Move the input file to another directory to archive it
	 */
	public static void renameInputFile(Path inDirectory) {

		File file = inDirectory.resolve("kraftwerk.json").toFile();
		String fileWithTime = "kraftwerk-" + DateUtils.getCurrentTimeStamp() + ".json";
		File file2 = inDirectory.resolve(fileWithTime).toFile();
		if (file2.exists()) {
			log.warn(String.format("Trying to rename '%s' to '%s', but second file already exists.", file, file2));
			log.warn("Timestamped input file will be over-written.");
			try {
				Files.delete(file2.toPath());
			} catch (IOException e) {
				log.error("Can't delete file {}, IOException : {}", fileWithTime, e.getMessage());
			}
		}
		if(!file.renameTo(file2)){
			log.error("Can't rename file {} to {}", file.getName(), file2.getName());
		}
	}

	public static void archiveInputFiles(UserInputsFile userInputsFile) throws KraftwerkException {
		//
		Path inputFolder = userInputsFile.getInputDirectory();
		String[] directories = inputFolder.toString().split(Pattern.quote(File.separator));
		String campaignName = directories[directories.length - 1];

		createArchiveDirectoryIfNotExists(inputFolder);

		//
		for (String mode : userInputsFile.getModes()) {
			ModeInputs modeInputs = userInputsFile.getModeInputs(mode);
			archiveData(inputFolder, campaignName, modeInputs);
			archiveParadata(inputFolder, campaignName, modeInputs);
			archiveReportingData(inputFolder, campaignName, modeInputs);
		}
	}

	/**
	 *  If reporting data, we move reporting data files
	 * @param inputFolder
	 * @param campaignName
	 * @param modeInputs
	 * @throws KraftwerkException
	 */
	private static void archiveReportingData(Path inputFolder, String campaignName, ModeInputs modeInputs)
			throws KraftwerkException {
		if (modeInputs.getReportingDataFile() != null) {
			moveDirectory(modeInputs.getReportingDataFile().toFile(), inputFolder.resolve(ARCHIVE)
					.resolve(getRoot(modeInputs.getReportingDataFile(), campaignName)).toFile());
		}
	}

	/**
	 * If paradata, we move the paradata folder
	 * @param inputFolder
	 * @param campaignName
	 * @param modeInputs
	 * @throws KraftwerkException
	 */
	private static void archiveParadata(Path inputFolder, String campaignName, ModeInputs modeInputs)
			throws KraftwerkException {
		if (modeInputs.getParadataFolder() != null) {
			moveDirectory(modeInputs.getParadataFolder().toFile(), inputFolder.resolve(ARCHIVE)
					.resolve(getRoot(modeInputs.getParadataFolder(), campaignName)).toFile());
		}
	}

	private static void archiveData(Path inputFolder, String campaignName, ModeInputs modeInputs)
			throws KraftwerkException {
		Path dataPath = modeInputs.getDataFile();
		Path newDataPath = inputFolder.resolve(ARCHIVE).resolve(getRoot(dataPath, campaignName));

		if (!Files.exists(newDataPath)) {
			new File(newDataPath.getParent().toString()).mkdirs();
		}
		if (Files.isRegularFile(dataPath)) {
			moveFile(dataPath, newDataPath);
		} else if (Files.isDirectory(dataPath)) {
			moveDirectory(dataPath.toFile(),newDataPath.toFile());
		} else {
			log.debug(String.format("No file or directory at path: %s", dataPath));
		}
	}

	private static void createArchiveDirectoryIfNotExists(Path inputFolder) {
		if (!Files.exists(inputFolder.resolve(ARCHIVE))) {
			inputFolder.resolve(ARCHIVE).toFile().mkdir();
		}
	}

	private static void moveFile(Path dataPath, Path newDataPath) throws KraftwerkException {
		try {
			Files.move(dataPath, newDataPath);
		} catch (IOException e) {
			throw new KraftwerkException(500, "Can't move file " + dataPath + " to " + newDataPath);
		}
	}


	private static void moveDirectory(File sourceFile, File destFile) throws KraftwerkException {
		if (sourceFile.isDirectory()) {
			File[] files = sourceFile.listFiles();
			assert files != null : "List of files in sourceFile is null";
			for (File file : files)
				moveDirectory(file, new File(destFile, file.getName()));
			try {
				Files.delete(sourceFile.toPath());
			} catch (IOException e) {
				throw new KraftwerkException(500, "Can't delete " + sourceFile + " - IOException : " + e.getMessage());
			}
		} else {
			if (!destFile.getParentFile().exists() && !destFile.getParentFile().mkdirs())
				throw new KraftwerkException(500, "Can't create directory to archive");
			if (!sourceFile.renameTo(destFile))
				throw new KraftwerkException(500, "Can't rename file " + sourceFile + " to " + destFile);
		}
	}
	
	private static String getRoot(Path path, String campaignName) {
		String[] directories = path.toString().split(Pattern.quote(File.separator));
		int campaignIndex = Arrays.asList(directories).indexOf(campaignName);
		String[] newDirectories = Arrays.copyOfRange(directories, campaignIndex + 1, directories.length);
		StringBuilder result = new StringBuilder();
		String sep = "";
		for (String directory : newDirectories) {
			result.append(sep).append(directory);
			sep = File.separator;
		}
		return result.toString();
	}
	
	
	public static void deleteDirectory(Path directoryPath) throws KraftwerkException {
		try {
			FileSystemUtils.deleteRecursively(directoryPath);
		} catch (IOException e) {
			throw new KraftwerkException(500, "IOException when deleting temp folder : "+e.getMessage());
		}
	}

	/**
	 * List the files in the directory
	 * @param dir
	 * @return
	 */
	public static List<String> listFiles(String dir) {
		return Stream.of(new File(dir).listFiles())
				.filter(file -> !file.isDirectory())
				.map(File::getName)
				.toList();
	}
	
	public static Path getTempVtlFilePath(UserInputs userInputs, String step, String dataset) {
		createDirectoryIfNotExist(FileUtils.transformToTemp(userInputs.getInputDirectory()));
		return FileUtils.transformToTemp(userInputs.getInputDirectory()).resolve(step+ dataset+".vtl");
	}
	
	public static void createDirectoryIfNotExist(Path path) {
		try {
			Files.createDirectories(path);
			log.info(String.format("Created folder: %s", path.toFile().getAbsolutePath()));
		} catch (IOException e) {
			log.error("Permission refused to create folder: " + path.getParent(), e);
		}
	}

	public static Path convertToPath(String userField, Path inputDirectory) throws KraftwerkException {
		if (userField != null && !"null".equals(userField) && !userField.isEmpty()) {
			Path inputPath = inputDirectory.resolve(userField);
			if (!new File(inputPath.toUri()).exists()) {
				throw new KraftwerkException(400, String.format("The input folder \"%s\" does not exist in \"%s\".", userField, inputDirectory.toString()));
			}
			return inputPath;
		} else {
			return null;
		}
	}

	public static URL convertToUrl(String userField, Path inputDirectory) {
		if (userField == null) {
			log.debug("null value out of method that reads DDI field (should not happen).");
			return null;
		}
		try {
			if (userField.startsWith("http")) {
				return new URI(userField).toURL();
			}
			return inputDirectory.resolve(userField).toFile().toURI().toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			log.error("Unable to convert URL from user input: " + userField);
			return null;
		} 
	}
	
}
