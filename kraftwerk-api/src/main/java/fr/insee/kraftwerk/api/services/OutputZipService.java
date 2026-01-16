package fr.insee.kraftwerk.api.services;


import fr.insee.kraftwerk.core.encryption.EncryptionUtils;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutputZipService {

    private static final String ZIP_EXTENSION = ".zip";
    private static final String TMPDIR = "java.io.tmpdir";

    private final EncryptionUtils encryptionUtils;

    /**
     * Create a zip of kraftwerkExecutionContext.outDirectory, then encrypt the zip if kraftwerkExecutionContext.withEncryption=true.
     */
    public void encryptAndArchiveOutputs(KraftwerkExecutionContext kraftwerkExecutionContext,
                                         FileUtilsInterface fileUtils) throws KraftwerkException {

        if (!kraftwerkExecutionContext.isWithEncryption()) {
            return;
        }
        Path outDirectory = kraftwerkExecutionContext.getOutDirectory();
        if (outDirectory == null) throw new KraftwerkException(500, "outDirectory is null in context");

        Path tempZipFile = null;
        Path tempEncFile = null;

        try {
            Path tempDirectory = Path.of(System.getProperty(TMPDIR));
            Files.createDirectories(tempDirectory);

            String baseName = outDirectory.getFileName().toString();
            tempZipFile = Files.createTempFile(tempDirectory, baseName + "_", ZIP_EXTENSION);

            buildZip(outDirectory, tempZipFile, fileUtils);

            String encExt = encryptionUtils.getEncryptedFileExtension();
            if (!encExt.startsWith(".")) encExt = "." + encExt;

            tempEncFile = Files.createTempFile(tempDirectory, baseName + "_", ZIP_EXTENSION + encExt);

            try (InputStream encrypted = encryptionUtils.encryptOutputFile(tempZipFile, kraftwerkExecutionContext);
                 OutputStream out = Files.newOutputStream(tempEncFile,
                         StandardOpenOption.TRUNCATE_EXISTING)) {
                encrypted.transferTo(out);
            }

            Path parent = outDirectory.getParent();
            if (parent == null) throw new KraftwerkException(500, "Cannot resolve parent of outDirectory: " + outDirectory);

            String targetEncPath = parent.resolve(baseName + ZIP_EXTENSION + encExt).toString();

            // store encrypted file:
            fileUtils.moveFile(tempEncFile, targetEncPath);
            tempEncFile = null;

            deleteWithRetry(tempZipFile);
            tempZipFile = null;

            fileUtils.deleteDirectory(outDirectory);

            log.info("Encrypted archive created at {} and outDirectory deleted ({})", targetEncPath, outDirectory);

        } catch (UncheckedIOException | IOException e) {
            cleanupTemps(tempZipFile, tempEncFile);
            Throwable cause = e instanceof UncheckedIOException ? e.getCause() : e;
            throw new KraftwerkException(500, "IO error during output archive: " + cause.getMessage());
        }

    }

    private void cleanupTemps(Path zip, Path enc) {
        if (enc != null) deleteWithRetry(enc);
        if (zip != null) deleteWithRetry(zip);
    }


    /**
     * Builds the zip at zipFile from outDir, supporting both:
     * - filesystem outDir (exists locally)
     * - Minio outDir (prefix)
     */
    private void buildZip(Path outDirectory, Path zipFile, FileUtilsInterface fileUtils) throws KraftwerkException, IOException {
        Files.deleteIfExists(zipFile);

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(
                Files.newOutputStream(zipFile, StandardOpenOption.CREATE_NEW))) {

            if (Files.exists(outDirectory) && Files.isDirectory(outDirectory)) {
                try (var paths = Files.walk(outDirectory)) {
                    paths.filter(Files::isRegularFile).forEach(path -> {
                        try {
                            addFileToZip(outDirectory, path, zipOutputStream);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });

                }
            } else {
                String prefix = normalizePrefix(outDirectory.toString());
                List<String> objects = fileUtils.listFileNames(prefix);
                for (String objectPath : objects) {
                    if (objectPath.endsWith("/")) continue;
                    addMinioObjectToZip(prefix, objectPath, zipOutputStream, fileUtils);
                }
            }
        }
    }

    private void addFileToZip(Path rootDir, Path file, ZipOutputStream zipOutputStream) throws IOException {
        String entryName = rootDir.relativize(file).toString().replace("\\", "/");
        zipOutputStream.putNextEntry(new ZipEntry(entryName));
        Files.copy(file, zipOutputStream);
        zipOutputStream.closeEntry();
    }


    private void addMinioObjectToZip(String prefix,
                                     String objectPath,
                                     ZipOutputStream zipOutputStream,
                                     FileUtilsInterface fileUtils) throws KraftwerkException {

        String entryName = relativize(prefix, objectPath);
        if (entryName.isBlank()) return;

        try (InputStream inputStream = fileUtils.readFile(objectPath)) {
            if (inputStream == null) {
                log.warn("Cannot read object {}, skipping", objectPath);
                return;
            }
            zipOutputStream.putNextEntry(new ZipEntry(entryName));
            inputStream.transferTo(zipOutputStream);
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            throw new KraftwerkException(500, "IO error zipping object " + objectPath + ": " + e.getMessage());
        }
    }

    private String normalizePrefix(String p) {
        String s = p.replace("\\", "/");
        return s.endsWith("/") ? s : s + "/";
    }

    private String relativize(String basePath, String fullPath) {
        String normalizedFullPath = fullPath.replace("\\", "/");
        String normalizedBasePath = basePath.replace("\\", "/");
        if (normalizedFullPath.startsWith(normalizedBasePath)) return normalizedFullPath.substring(normalizedBasePath.length());

        String basePathWithoutTrailingSlash = normalizedBasePath.endsWith("/") ? normalizedBasePath.substring(0, normalizedBasePath.length() - 1) : normalizedBasePath;
        if (normalizedFullPath.startsWith(basePathWithoutTrailingSlash)) {
            String relativePath = normalizedFullPath.substring(basePathWithoutTrailingSlash.length());
            if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);
            return relativePath;
        }
        return normalizedFullPath;
    }

    private void deleteWithRetry(Path path) {
        for (int i = 0; i < 8; i++) {
            try {
                Files.deleteIfExists(path);
                return;
            } catch (IOException e) {
                try { Thread.sleep(80L); } catch (InterruptedException ignored) {}
            }
        }
        path.toFile().deleteOnExit();
        log.warn("Could not delete temp file (locked), will deleteOnExit: {}", path);
    }

}