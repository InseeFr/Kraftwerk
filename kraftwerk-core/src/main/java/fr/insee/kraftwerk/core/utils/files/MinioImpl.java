package fr.insee.kraftwerk.core.utils.files;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.utils.DateUtils;
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
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
        try {
            moveFile(Path.of(file1MinioPathString), file2MinioPathString);
        }catch (Exception e){
            log.error(e.toString());
        }
    }

    @Override
    public void archiveInputFiles(UserInputsFile userInputsFile) throws KraftwerkException {
        //
        Path inputFolder = userInputsFile.getInputDirectory();
        String[] directories = inputFolder.toString().split("/");
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
            for (String filePath : listFileNames(directoryPath.toString())) {
                deleteFile(filePath);
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    @Override
    public List<String> listFileNames(String dir) {
        try {
            ArrayList<String> filePaths = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(dir.replace("\\","/")).recursive(true).build());

            for (Result<Item> result : results) {
                filePaths.add(result.get().objectName());
            }
            return filePaths;
        } catch (Exception e) {
            log.error(e.toString());
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> listFilePaths(String dir) {
        return listFileNames(dir.replace("\\","/"));
    }

    @Override
    public void createDirectoryIfNotExist(Path path) {
        //MinIO creates automatically the folders
    }

    @Override
    public Path getTempVtlFilePath(UserInputs userInputs, String step, String dataset) {
        return FileUtilsInterface.transformToTemp(userInputs.getInputDirectory()).resolve(step+ dataset+".vtl");
    }

    @Override
    public Boolean isDirectory(String path){
        try {
            //List files of parent to check if directory
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(Path.of(path).getParent().toString().replace("\\","/")).recursive(true).build());

            for (Result<Item> result : results) {
                if(result.get().objectName().startsWith(path)){
                    return true;
                }
            }
            log.warn("S3 File or folder {} not found in {}", Path.of(path).getFileName().toString().replace("\\","/"), Path.of(path).getParent().toString().replace("\\","/"));
            return null;
        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }

    @Override
    public long getSizeOf(String path) {
        try {
            StatObjectResponse objectStat = minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(path.replace("\\","/")).build());
            return objectStat.size();
        }catch (Exception e){
            log.error(e.toString());
            return 0;
        }
    }

    @Override
    public void writeFile(String path, String toWrite, boolean replace) {
        InputStream inputStream = new ByteArrayInputStream(toWrite.getBytes());
        writeFileOnMinio(path.replace("\\","/"), inputStream, toWrite.length(), replace);
    }

    @Override
    public void writeFile(String path, InputStream inputStream, boolean replace) {
        writeFileOnMinio(path.replace("\\","/"), inputStream, replace);
    }

    @Override
    public String findFile(String directory, String fileRegex) throws KraftwerkException {
        //Stream of files with filename matching fileRegex
        try (Stream<String> files = listFileNames(directory.replace("\\","/"))
                .stream().filter(s -> s.split("/")[s.split("/").length - 1].matches(fileRegex))) {
            return files.findFirst()
                    .orElseThrow(() -> new KraftwerkException(404, "No file with regex " + fileRegex + " found in " + directory.replace("\\","/")));
        }
    }

    @Override
    public InputStream readFile(String minioPath) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(minioPath.replace("\\","/")).build());
        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }

    @Override
    public boolean isFileExists(String objectPath) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectPath.replace("\\","/")).build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }


    @Override
    public void moveFile(Path fileSystemPath, String dstMinioPath) throws KraftwerkException {
        try (InputStream inputStream = new FileInputStream(fileSystemPath.toFile())){
            writeFileOnMinio(dstMinioPath.replace("\\","/"), inputStream, Files.size(fileSystemPath), true);
        } catch (Exception e) {
            throw new KraftwerkException(500, "Can't move file " + fileSystemPath + " to " + dstMinioPath.replace("\\","/"));
        }
        try {
            Files.deleteIfExists(fileSystemPath);
        }catch (Exception e) {
            log.error("Error during file system file deletion : {}", e.getMessage());
        }
    }

    @Override
    public Path convertToPath(String userField, Path inputDirectory) throws KraftwerkException {
        if (userField != null && !"null".equals(userField) && !userField.isEmpty()) {
            Path inputPath = inputDirectory.resolve(userField);
            if (Boolean.FALSE.equals(isDirectory(inputPath.toString().replace("\\","/")))
                || isDirectory(inputPath.toString().replace("\\","/")) == null
            ) {
                throw new KraftwerkException(400, String.format("The input folder \"%s\" does not exist in \"%s\".", userField, inputDirectory));
            }
            return inputPath;
        } else {
            return null;
        }
    }

    @Override
    public String convertToUrl(String userField, Path inputDirectory) {
        if (userField == null) {
            return null;
        }
        try {
            if (userField.startsWith("http")) {
                return new URI(userField).toURL().toString();
            }
            return inputDirectory.resolve(userField).toString();
        } catch (MalformedURLException | URISyntaxException e) {
            return null;
        }
    }

    //Utilities

    private void writeFileOnMinio(String minioPath, InputStream inputStream, long fileSize, boolean replace) {
        try (InputStream alreadyExistingInputStream = readFile(minioPath)){
            if(replace || !isFileExists(minioPath)){
                minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).stream(inputStream, fileSize, -1).object(minioPath.replace("\\","/")).build());
                return;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            alreadyExistingInputStream.transferTo(baos);
            inputStream.transferTo(baos);
            inputStream.close();
            InputStream appendedInputStream = new ByteArrayInputStream(baos.toByteArray());
            int size = baos.size();
            baos.close();
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).stream(appendedInputStream, size, -1).object(minioPath.replace("\\","/")).build());
            appendedInputStream.close();
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    private void writeFileOnMinio(String minioPath, InputStream inputStream, boolean replace) {
        try (InputStream alreadyExistingInputStream = readFile(minioPath)){
            if(replace || !isFileExists(minioPath)){
                minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).stream(inputStream, -1, 10485760).object(minioPath.replace("\\","/")).build());
                return;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            alreadyExistingInputStream.transferTo(baos);
            inputStream.transferTo(baos);
            inputStream.close();
            InputStream appendedInputStream = new ByteArrayInputStream(baos.toByteArray());
            int size = baos.size();
            baos.close();
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).stream(appendedInputStream, size, -1).object(minioPath.replace("\\","/")).build());
            appendedInputStream.close();
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    private void moveDirectory(String srcMinioPath, String dstMinioPath) {
        try {
            for (String filePath : listFileNames(srcMinioPath.replace("\\","/"))) {
                moveFile(Path.of(filePath), dstMinioPath + "/" + extractFileName(filePath));
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        return Path.of(filePath).getFileName().toString().replace("\\","/");
    }

    private void deleteFile(String minioPath) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(minioPath.replace("\\","/")).build());
        } catch (Exception e) {
            log.error(e.toString());
        }
    }



    private void archiveData(Path inputFolder, String campaignName, ModeInputs modeInputs) throws KraftwerkException{
        Path dataPath = modeInputs.getDataFile();
        Path newDataPath = inputFolder.resolve(ARCHIVE).resolve(getRoot(dataPath, campaignName));

        if (Boolean.FALSE.equals(isDirectory(dataPath.toString()))) {
            moveFile(dataPath, newDataPath.toString());
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
                    .resolve(getRoot(modeInputs.getParadataFolder(), campaignName)).toString().replace("\\","/"));
        }
    }

    /**
     *  If reporting data, we move reporting data files
     */
    private void archiveReportingData(Path inputFolder, String campaignName, ModeInputs modeInputs){
        if (modeInputs.getReportingDataFile() != null) {
            moveDirectory(modeInputs.getReportingDataFile().toString(), inputFolder.resolve(ARCHIVE)
                    .resolve(getRoot(modeInputs.getReportingDataFile(), campaignName)).toString().replace("\\","/"));
        }
    }

    private String getRoot(Path path, String campaignName) {
        String[] directories = path.toString().split("/");
        int campaignIndex = Arrays.asList(directories).indexOf(campaignName);
        String[] newDirectories = Arrays.copyOfRange(directories, campaignIndex + 1, directories.length);
        StringBuilder result = new StringBuilder();
        String sep = "";
        for (String directory : newDirectories) {
            result.append(sep).append(directory);
            sep = "/";
        }
        return result.toString().replace("\\","/");
    }
}
