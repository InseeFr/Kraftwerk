package fr.insee.kraftwerk.core.encryption;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;

import java.io.InputStream;
import java.nio.file.Path;

public interface EncryptionUtils {


        InputStream encryptOutputFile(
                Path pathOfFileToEncrypt, KraftwerkExecutionContext kraftwerkExecutionContext
        ) throws KraftwerkException;

        String getEncryptedFileExtension();
}
