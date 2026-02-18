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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static fr.insee.kraftwerk.core.Constants.ENCRYPTED_FILE_EXTENSION;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutputZipService {

    private static final String ZIP_EXTENSION = ".zip";
    private static final String TMPDIR_PROPERTY = "java.io.tmpdir";

    private final EncryptionUtils encryptionUtils;

    /**
     * Archives outputs only when encryption is enabled.
     *
     * <p>If withEncryption=false: no-op (keeps outDirectory as-is).</p>
     *
     * <p>If withEncryption=true:</p>
     * <ul>
     *   <li>creates a zip from outDirectory (local directory or Minio/S3 prefix),</li>
     *   <li>encrypts it into outDirectoryName.zip.enc next to outDirectory (same parent),</li>
     *   <li>then deletes outDirectory.</li>
     * </ul>
     */
    public void encryptAndArchiveOutputs(KraftwerkExecutionContext kraftwerkExecutionContext,
                                         FileUtilsInterface fileUtils) throws KraftwerkException {

        if (!kraftwerkExecutionContext.isWithEncryption()) {
            log.debug("withEncryption=false -> skip archive/encryption");
            return;
        }
        Path outDirectory = requireOutDirectory(kraftwerkExecutionContext);

        Path tempZipFile = null;
        Path tempEncFile = null;

        try {
            Path tmpDir = Path.of(System.getProperty(TMPDIR_PROPERTY));
            Files.createDirectories(tmpDir);
            String baseName = outDirectory.getFileName().toString();

            tempZipFile = Files.createTempFile(tmpDir, baseName + "_", ZIP_EXTENSION);
            buildZip(outDirectory, tempZipFile, fileUtils);

            tempEncFile = Files.createTempFile(tmpDir, baseName + "_", ZIP_EXTENSION + ENCRYPTED_FILE_EXTENSION);

            encryptZipToEncryptedFile(tempZipFile, tempEncFile, kraftwerkExecutionContext);

            String targetEncPath = resolveTargetEncPath(outDirectory, baseName, ENCRYPTED_FILE_EXTENSION);

            fileUtils.moveFile(tempEncFile, targetEncPath);
            tempEncFile = null;

            deleteWithRetry(tempZipFile);
            tempZipFile = null;

            fileUtils.deleteDirectory(outDirectory);

            log.info("Encrypted archive created at {}", targetEncPath);
            log.info("Deleted outDirectory {}", outDirectory);

        } catch (IOException e) {
            cleanupTemps(tempZipFile, tempEncFile);
            throw new KraftwerkException(500, "IO error during output archive: " + e.getMessage());
        }
    }

    /**
     * Encrypts a local zip file into a local encrypted file.
     */
    private void encryptZipToEncryptedFile(Path zipFile, Path encFile, KraftwerkExecutionContext kraftwerkExecutionContext)
            throws IOException, KraftwerkException {
        try (InputStream encrypted = encryptionUtils.encryptOutputFile(zipFile, kraftwerkExecutionContext);
             OutputStream out = Files.newOutputStream(encFile, StandardOpenOption.TRUNCATE_EXISTING)) {
            encrypted.transferTo(out);
        }
    }

    private String resolveTargetEncPath(Path outDirectory, String baseName, String encExt) throws KraftwerkException {
        Path parent = outDirectory.getParent();
        if (parent == null) {
            throw new KraftwerkException(500, "Cannot resolve parent of outDirectory: " + outDirectory);
        }
        return parent.resolve(baseName + ZIP_EXTENSION + encExt).toString();
    }

    /**
     * Validates and returns outDirectory from the execution context.
     */
    private Path requireOutDirectory(KraftwerkExecutionContext kraftwerkExecutionContext) throws KraftwerkException {
        Path outDirectory = kraftwerkExecutionContext.getOutDirectory();
        if (outDirectory == null) {
            throw new KraftwerkException(500, "outDirectory is null in context");
        }
        return outDirectory;
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
    private void buildZip(Path outDirectory, Path zipFile, FileUtilsInterface fileUtils)
            throws KraftwerkException, IOException {

        Files.deleteIfExists(zipFile);

        try (ZipOutputStream zos = new ZipOutputStream(
                Files.newOutputStream(zipFile, StandardOpenOption.CREATE_NEW))) {

            if (Files.isDirectory(outDirectory)) {
                zipLocalDirectory(outDirectory, zos);
                return;
            }

            if (Files.exists(outDirectory)) {
                throw new KraftwerkException(400, "outDirectory must be a directory, got: " + outDirectory);
            }

            zipMinioPrefix(outDirectory, zos, fileUtils);
        }
    }

    private void zipLocalDirectory(Path outDirectory, ZipOutputStream zos) throws IOException {
        try (var paths = Files.walk(outDirectory)) {
            for (Path path : (Iterable<Path>) paths::iterator) {
                if (!Files.isRegularFile(path)) {
                    continue;
                }
                addFileToZip(outDirectory, path, zos);
            }
        }
    }

    private void zipMinioPrefix(Path outDirectory, ZipOutputStream zos, FileUtilsInterface fileUtils)
            throws KraftwerkException {

        String prefix = normalizePrefix(outDirectory.toString());
        List<String> objects = fileUtils.listFileNames(prefix);

        if (objects == null || objects.isEmpty()) {
            throw new KraftwerkException(404, "No objects found for Minio prefix: " + prefix);
        }

        for (String objectPath : objects) {
            if (objectPath.endsWith("/")) continue;
            addMinioObjectToZip(prefix, objectPath, zos, fileUtils);
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

    /**
     * Normalizes a Minio prefix: uses '/' separators and ensures it ends with '/'.
     */
    private String normalizePrefix(String p) {
        String s = p.replace("\\", "/");
        return s.endsWith("/") ? s : s + "/";
    }

    /**
     * Removes the Minio/S3 prefix from an object path to build the zip entry name.
     * Example: prefix="out/campaign/" object="out/campaign/a/b.csv" -> "a/b.csv"
     */
    private String relativize(String prefix, String objectPath) {
        String normalizedPrefix = normalizePrefix(prefix);
        String normalizedObject = objectPath.replace("\\", "/");

        if (normalizedObject.startsWith(normalizedPrefix)) {
            return normalizedObject.substring(normalizedPrefix.length());
        }
        return normalizedObject;
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