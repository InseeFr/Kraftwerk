package cucumber;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.libjavachiffrement.config.CipherConfig;
import fr.insee.libjavachiffrement.symmetric.SymmetricEncryptionEndpoint;
import fr.insee.libjavachiffrement.symmetric.SymmetricKeyContext;
import fr.insee.libjavachiffrement.vault.VaultConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Class to create static variables giving path for test resources.
 */
public class TestConstants {


    public static @NotNull SymmetricEncryptionEndpoint getSymmetricEncryptionEndpointForTest(KraftwerkExecutionContext kraftwerkExecutionContext) {
        VaultConfig vaultConfigTest = new VaultConfig(
                kraftwerkExecutionContext.getVaultContext().getVaultCaller(),
                kraftwerkExecutionContext.getVaultContext().getVaultPath(),
                "VAULT_NAME",
                "VAULT_PROPERTY_NAME");
        CipherConfig cipherConfig = new CipherConfig(null, null, vaultConfigTest);
        SymmetricKeyContext keyContext = new SymmetricKeyContext( String.format(Constants.STRING_FORMAT_VAULT_PATH,
                Constants.TRUST_VAULT_PATH,
                Constants.TRUST_AES_KEY_VAULT_PATH));

        return new SymmetricEncryptionEndpoint(keyContext,cipherConfig);
    }
}