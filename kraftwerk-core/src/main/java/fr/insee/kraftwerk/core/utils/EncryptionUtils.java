package fr.insee.kraftwerk.core.utils;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.libjavachiffrement.config.SymmetricEncryptionConfig;
import fr.insee.libjavachiffrement.core.symmetricencryption.SymmetricEncryptionEndpoint;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
@Component
public class EncryptionUtils {
    public static final String ENCRYPTED_FILE_EXTENSION = ".enc";
    private static final String VAULT_NAME = "filiere_enquetes";
    private static final String VAULT_PROPERTY_NAME = "value";

    private static final int ENCRYPTION_BUFFER_SIZE = 8192;


    /**
     * Encrypts a output file
     * @param tmpOutputFile temporary Kraftwerk output file to encrypt
     * @param fileUtilsInterface file system interface to use to save file
     * @throws KraftwerkException if any problem
     */
    public static void encryptOutputFile(
            String tmpOutputFile,
            String outputFile,
            FileUtilsInterface fileUtilsInterface,
            KraftwerkExecutionContext kraftwerkExecutionContext
    ) throws KraftwerkException {
        //Check if vault parameters are in context
        if(kraftwerkExecutionContext.getVaultContext().getVaultCaller() == null){
            throw new KraftwerkException(500, "Cannot encrypt, Vault is not configured properly : Vault caller null");
        }
        if(kraftwerkExecutionContext.getVaultContext().getVaultPath() == null){
            throw new KraftwerkException(500, "Cannot encrypt, Vault is not configured properly : Vault path null");
        }

        SymmetricEncryptionEndpoint symmetricEncryptionEndpoint = getSymmetricEncryptionEndpoint(kraftwerkExecutionContext);
        Path tmpOutputFilePath = Path.of(tmpOutputFile);

        try(FileInputStream fileInputStream = new FileInputStream(tmpOutputFilePath.toFile());
            InputStream outInputStream = symmetricEncryptionEndpoint.getEncryptedInputStream(fileInputStream,
                    ENCRYPTION_BUFFER_SIZE)
        ){
            fileUtilsInterface.writeFile(outputFile, outInputStream, true);
        }catch (IOException e){
            throw new KraftwerkException(500,
                    "IO Exception during encryption : %s !".formatted(e.toString()));
        }
        try{
            Files.delete(tmpOutputFilePath);
        } catch (IOException e) {
            throw new KraftwerkException(500,
                    "IO Exception during temp file deletion : %s !".formatted(e.toString()));
        }

    }

    @NotNull
    private static SymmetricEncryptionEndpoint getSymmetricEncryptionEndpoint(KraftwerkExecutionContext kraftwerkExecutionContext) {
        SymmetricEncryptionConfig symmetricEncryptionConfig = new SymmetricEncryptionConfig(
                kraftwerkExecutionContext.getVaultContext().getVaultCaller(),
                kraftwerkExecutionContext.getVaultContext().getVaultPath(),
                VAULT_NAME,
                VAULT_PROPERTY_NAME,
                String.format(Constants.STRING_FORMAT_VAULT_PATH,
                        Constants.TRUST_VAULT_PATH,
                        Constants.TRUST_AES_KEY_VAULT_PATH)
        );
        return new SymmetricEncryptionEndpoint(symmetricEncryptionConfig);
    }
}
