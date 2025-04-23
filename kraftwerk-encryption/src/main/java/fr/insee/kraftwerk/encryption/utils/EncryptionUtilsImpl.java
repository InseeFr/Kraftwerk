package fr.insee.kraftwerk.encryption.utils;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.encryption.EncryptionUtils;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.libjavachiffrement.config.CipherConfig;
import fr.insee.libjavachiffrement.symmetric.SymmetricEncryptionEndpoint;
import fr.insee.libjavachiffrement.symmetric.SymmetricKeyContext;
import fr.insee.libjavachiffrement.vault.VaultCaller;
import fr.insee.libjavachiffrement.vault.VaultConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Component
@Slf4j
public class EncryptionUtilsImpl implements EncryptionUtils {
    private static final String VAULT_NAME = "filiere_enquetes";
    private static final String VAULT_PROPERTY_NAME = "value";

    public static final String ENCRYPTED_FILE_EXTENSION = ".enc";

    private static final int ENCRYPTION_BUFFER_SIZE = 8192;

    private final VaultCaller vaultCaller;
    @NonNull
    private final String vaultPath;

    public EncryptionUtilsImpl(@Value("${fr.insee.kraftwerk.encryption.vault.app-role.role-id}") String roleId, @Value("${fr.insee.kraftwerk.encryption.vault.app-role.secret-id}") String secretId, @Value("${fr.insee.kraftwerk.encryption.vault.uri}") String vaultPath) {
        log.debug("vaultPath : {}", vaultPath);
        this.vaultPath = vaultPath;
        this.vaultCaller =  new VaultCaller(
                roleId,
                secretId,
                Constants.VAULT_APPROLE_ENDPOINT
        );

    }


    /**
     * Encrypts an output file
     * @param pathOfFileToEncrypt temporary Kraftwerk output file to encrypt
     * @param kraftwerkExecutionContext Context
     * @throws KraftwerkException if any problem
     */
    public InputStream encryptOutputFile(
            Path pathOfFileToEncrypt, KraftwerkExecutionContext kraftwerkExecutionContext
    ) throws KraftwerkException {
        SymmetricEncryptionEndpoint symmetricEncryptionEndpoint = getSymmetricEncryptionEndpoint();

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
    private SymmetricEncryptionEndpoint getSymmetricEncryptionEndpoint() {
        CipherConfig cipherConfig =new CipherConfig(
                null,
                null,
                new VaultConfig(
                        vaultCaller,
                        vaultPath,
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
