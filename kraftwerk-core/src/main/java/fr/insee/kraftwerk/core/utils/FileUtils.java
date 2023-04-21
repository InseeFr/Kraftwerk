package fr.insee.kraftwerk.core.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.FileSystemUtils;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FileUtils {


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

	public static void moveInputFiles(UserInputs userInputs) throws KraftwerkException {
		//
		Path inputFolder = userInputs.getInputDirectory();
		String[] directories = inputFolder.toString().split(Pattern.quote(File.separator));
		String campaignName = directories[directories.length - 1];

		// First we create an archive directory in case it doesn't exist
		if (!Files.exists(inputFolder.resolve(ARCHIVE))) {
			inputFolder.resolve(ARCHIVE).toFile().mkdir();
		}

		//
		for (String mode : userInputs.getModes()) {
			ModeInputs modeInputs = userInputs.getModeInputs(mode);

			// Move data file or folder in the archive
			// MANAGE DATA
			Path dataPath = modeInputs.getDataFile();
			Path newDataPath = inputFolder.resolve(ARCHIVE).resolve(getRoot(dataPath, campaignName));

			if (!Files.exists(newDataPath)) {
				new File(newDataPath.getParent().toString()).mkdirs();
			}
			if (Files.isRegularFile(dataPath)) {
				moveFile(dataPath, newDataPath);
			} else if (Files.isDirectory(dataPath)) {
				moveDirectory(dataPath.toFile(),
						inputFolder.resolve(ARCHIVE).resolve(getRoot(dataPath, campaignName)).toFile());
			} else {
				log.debug(String.format("No file or directory at path: %s", dataPath));
			}

			// MANAGE PARADATA
			// If paradata, we move the paradata folder
			if (modeInputs.getParadataFolder() != null) {
				moveDirectory(modeInputs.getParadataFolder().toFile(), inputFolder.resolve(ARCHIVE)
						.resolve(getRoot(modeInputs.getParadataFolder(), campaignName)).toFile());
			}

			// MANAGE REPORTINGDATA
			// If reporting data, we move reporting data files
			if (modeInputs.getReportingDataFile() != null) {
				if (!Files
						.exists(inputFolder.resolve(ARCHIVE).resolve(modeInputs.getReportingDataFile()).getParent())) {
					new File(getRoot(modeInputs.getReportingDataFile(), campaignName)).mkdirs();
				}
				moveDirectory(modeInputs.getReportingDataFile().getParent().toFile(), inputFolder.resolve(ARCHIVE)
						.resolve(getRoot(modeInputs.getReportingDataFile(), campaignName)).toFile());
				try {
					Files.delete(modeInputs.getReportingDataFile());
				} catch (IOException e) {
					throw new KraftwerkException(500, "Can't delete file " + modeInputs.getReportingDataFile());

				}
			}
		}
	}

	public static void moveFile(Path dataPath, Path newDataPath) throws KraftwerkException {
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
				.collect(Collectors.toList());
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
	
}
