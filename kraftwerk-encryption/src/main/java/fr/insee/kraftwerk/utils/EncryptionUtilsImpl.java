package fr.insee.kraftwerk.utils;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.encryption.EncryptionUtils;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.libjavachiffrement.config.CipherConfig;
import fr.insee.libjavachiffrement.symmetric.SymmetricEncryptionEndpoint;
import fr.insee.libjavachiffrement.symmetric.SymmetricKeyContext;
import fr.insee.libjavachiffrement.vault.VaultCaller;
import fr.insee.libjavachiffrement.vault.VaultConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Profile("!ci-public")
@Component
public class EncryptionUtilsImpl implements EncryptionUtils {
    private static final String VAULT_NAME = "filiere_enquetes";
    private static final String VAULT_PROPERTY_NAME = "value";

    public static final String ENCRYPTED_FILE_EXTENSION = ".enc";

    private static final int ENCRYPTION_BUFFER_SIZE = 8192;


    /**
     * Encrypts an output file
     * @param tmpOutputFile temporary Kraftwerk output file to encrypt
     * @param fileUtilsInterface file system interface to use to save file
     * @throws KraftwerkException if any problem
     */
    public InputStream encryptOutputFile(
            Path pathOfFileToEncrypt, KraftwerkExecutionContext kraftwerkExecutionContext
    ) throws KraftwerkException {
        //Check if vault parameters are in context
        if(kraftwerkExecutionContext.getVaultContext().getVaultCaller() == null){
            throw new KraftwerkException(500, "Cannot encrypt, Vault is not configured properly : Vault caller null");
        }
        if(kraftwerkExecutionContext.getVaultContext().getVaultPath() == null){
            throw new KraftwerkException(500, "Cannot encrypt, Vault is not configured properly : Vault path null");
        }

        SymmetricEncryptionEndpoint symmetricEncryptionEndpoint = getSymmetricEncryptionEndpoint(kraftwerkExecutionContext);

        try(FileInputStream fileInputStream = new FileInputStream(pathOfFileToEncrypt.toFile());
            InputStream outInputStream = symmetricEncryptionEndpoint.getEncryptedInputStream(fileInputStream,
                    ENCRYPTION_BUFFER_SIZE)
        ){
                return  outInputStream;
        }catch (IOException e){
            throw new KraftwerkException(500,
                    "IO Exception during encryption : %s !".formatted(e.toString()));
        }

    }

    @NotNull
    private static SymmetricEncryptionEndpoint getSymmetricEncryptionEndpoint(KraftwerkExecutionContext kraftwerkExecutionContext) {
        CipherConfig cipherConfig =new CipherConfig(
                null,
                null,
                new VaultConfig(
                        (VaultCaller) kraftwerkExecutionContext.getVaultContext().getVaultCaller(),
                        kraftwerkExecutionContext.getVaultContext().getVaultPath(),
                        VAULT_NAME,
                        VAULT_PROPERTY_NAME)
        );
        SymmetricKeyContext keyContext = new SymmetricKeyContext(
                String.format(Constants.STRING_FORMAT_VAULT_PATH,
                Constants.TRUST_VAULT_PATH,
                Constants.TRUST_AES_KEY_VAULT_PATH));
        return new SymmetricEncryptionEndpoint(keyContext, cipherConfig);
    }

    @Override
    public String getEncryptedFileExtension() {
        return ENCRYPTED_FILE_EXTENSION;
    }
}
