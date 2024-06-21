package fr.insee.kraftwerk.core.utils.files;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.utils.DateUtils;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@AllArgsConstructor
public class MinioImpl implements FileUtilsInterface {

    private static final String ARCHIVE = "Archive";

    MinioClient minioClient;
    String bucketName;

    // Interface methods

    @Override
    public void renameInputFile(Path inDirectory) {
        String file1MinioPathString = inDirectory.resolve("/kraftwerk.json").toString();
        String fileWithTime = "kraftwerk-" + DateUtils.getCurrentTimeStamp() + ".json";
        String file2MinioPathString = inDirectory.resolve(fileWithTime).toString();
        if (isFileExists(file2MinioPathString)) {
            log.warn(String.format("Trying to rename '%s' to '%s', but second file already exists.", Path.of(file1MinioPathString).getFileName(), Path.of(file2MinioPathString).getFileName()));
            log.warn("Timestamped input file will be over-written.");
            deleteFile(file2MinioPathString);
        }
        moveFile(file1MinioPathString, file2MinioPathString);
    }

    @Override
    public void archiveInputFiles(UserInputsFile userInputsFile) throws KraftwerkException {
        //
        Path inputFolder = userInputsFile.getInputDirectory();
        String[] directories = inputFolder.toString().split(Pattern.quote(File.separator));
        String campaignName = directories[directories.length - 1];

        //
        for (String mode : userInputsFile.getModes()) {
            ModeInputs modeInputs = userInputsFile.getModeInputs(mode);
            archiveData(inputFolder, campaignName, modeInputs);
            archiveParadata(inputFolder, campaignName, modeInputs);
            archiveReportingData(inputFolder, campaignName, modeInputs);
        }
    }

    @Override
    public void deleteDirectory(Path directoryPath) throws KraftwerkException {
        try {
            for (String filePath : listFiles(directoryPath.toString())) {
                deleteFile(filePath);
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    @Override
    public List<String> listFiles(String dir) {
        try {
            ArrayList<String> filePaths = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(dir).recursive(true).build());

            for (Result<Item> result : results) {
                filePaths.add(result.get().objectName());
            }
            return filePaths;
        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }

    @Override
    public List<String> listFilePaths(String dir) {
        return listFiles(dir);
    }

    @Override
    public Path getTempVtlFilePath(UserInputs userInputs, String step, String dataset) {
        return FileUtilsInterface.transformToTemp(userInputs.getInputDirectory()).resolve(step+ dataset+".vtl");
    }

    @Override
    public Path convertToPath(String userField, Path inputDirectory) throws KraftwerkException {
        if (userField != null && !"null".equals(userField) && !userField.isEmpty()) {
            return  inputDirectory.resolve(userField);
        } else {
            return null;
        }
    }

    @Override
    public URL convertToUrl(String userField, Path inputDirectory) {
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

    @Override
    public Boolean isDirectory(String path){
        try {
            //List files of parent to check if directory
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(Path.of(path).getParent().toString()).recursive(true).build());

            for (Result<Item> result : results) {
                if(result.get().objectName().equals(Path.of(path).getFileName().toString())){
                    return result.get().isDir();
                }
            }
            log.warn("S3 File or folder {} not found in {}", Path.of(path).getFileName().toString(), Path.of(path).getParent().toString());
            return null;
        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }

    @Override
    public long getSizeOf(String path) {
        try {
            StatObjectResponse objectStat = minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(path).build());
            return objectStat.size();
        }catch (Exception e){
            log.error(e.toString());
            return 0;
        }
    }

    @Override
    public void writeFile(String path, String toWrite, boolean replace) {
        InputStream inputStream = new ByteArrayInputStream(toWrite.getBytes());
        writeFileOnMinio(path, inputStream, toWrite.length());
    }

    @Override
    public String findFile(String directory, String fileRegex) throws KraftwerkException {
        return null; //TODO faire ça
    }

    @Override
    public InputStream readFile(String minioPath) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(minioPath).build());
        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }

    @Override
    public boolean isFileExists(String objectPath) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectPath).build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }

    //Utilities

    private void writeFileOnMinio(String minioPath, InputStream inputStream, int fileSize) {
        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).stream(inputStream, fileSize, -1).object(minioPath).build());
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    private void copyFile(String srcMinioPath, String dstMinioPath) {
        try {
            CopySource copySource = CopySource.builder().bucket(bucketName).object(srcMinioPath).build();
            minioClient.copyObject(CopyObjectArgs.builder().bucket(bucketName).object(dstMinioPath).source(copySource).build());
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    private void moveFile(String srcMinioPath, String dstMinioPath) {
        try {
            copyFile(srcMinioPath, dstMinioPath);
            deleteFile(srcMinioPath);
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    private void moveDirectory(String srcMinioPath, String dstMinioPath) {
        try {
            for (String filePath : listFiles(srcMinioPath)) {
                moveFile(filePath, dstMinioPath + "/" + extractFileName(filePath));
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        return Path.of(filePath).getFileName().toString();
    }

    private void deleteFile(String minioPath) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(minioPath).build());
        } catch (Exception e) {
            log.error(e.toString());
        }
    }



    private void archiveData(Path inputFolder, String campaignName, ModeInputs modeInputs) {
        Path dataPath = modeInputs.getDataFile();
        Path newDataPath = inputFolder.resolve(ARCHIVE).resolve(getRoot(dataPath, campaignName));

        if (!isDirectory(dataPath.toString())) {
            moveFile(dataPath.toString(), newDataPath.toString());
        } else {
            moveDirectory(dataPath.toString(), newDataPath.toString());
        }
    }

    /**
     * If paradata, we move the paradata folder
     */
    private void archiveParadata(Path inputFolder, String campaignName, ModeInputs modeInputs){
        if (modeInputs.getParadataFolder() != null) {
            moveDirectory(modeInputs.getParadataFolder().toString(), inputFolder.resolve(ARCHIVE)
                    .resolve(getRoot(modeInputs.getParadataFolder(), campaignName)).toString());
        }
    }

    /**
     *  If reporting data, we move reporting data files
     */
    private void archiveReportingData(Path inputFolder, String campaignName, ModeInputs modeInputs){
        if (modeInputs.getReportingDataFile() != null) {
            moveDirectory(modeInputs.getReportingDataFile().toString(), inputFolder.resolve(ARCHIVE)
                    .resolve(getRoot(modeInputs.getReportingDataFile(), campaignName)).toString());
        }
    }

    private String getRoot(Path path, String campaignName) {
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
}
