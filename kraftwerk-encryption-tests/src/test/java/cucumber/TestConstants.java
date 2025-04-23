package cucumber;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.libjavachiffrement.config.CipherConfig;
import fr.insee.libjavachiffrement.symmetric.SymmetricEncryptionEndpoint;
import fr.insee.libjavachiffrement.symmetric.SymmetricKeyContext;
import fr.insee.libjavachiffrement.vault.VaultCaller;
import fr.insee.libjavachiffrement.vault.VaultConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Class to create static variables giving path for test resources.
 */
public class TestConstants {

    public static final String TEST_RESOURCES_DIRECTORY = "src/test/resources";

    public static final String FUNCTIONAL_TESTS_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests";
    public static final String FUNCTIONAL_TESTS_INPUT_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests/in";
    public static final String FUNCTIONAL_TESTS_OUTPUT_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests/out";
    public static final String FUNCTIONAL_TESTS_TEMP_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests/temp";

    @NotNull
    public static KraftwerkExecutionContext getKraftwerkExecutionContext() {
        return new KraftwerkExecutionContext(
                null,
                false,
                true,
                true,
                false,
                419430400L,
                null
        );
    }


    @NotNull
    public static KraftwerkExecutionContext getKraftwerkExecutionContext(String inDirectory, boolean withEncryption) {
        return new KraftwerkExecutionContext(
                inDirectory,
                false,
                true,
                true,
                withEncryption,
                419430400L,
                withEncryption ?
                        new VaultContextStubForTests()
                        : null
        );
    }


    public static @NotNull SymmetricEncryptionEndpoint getSymmetricEncryptionEndpointForTest(KraftwerkExecutionContext kraftwerkExecutionContext) {
        VaultConfig vaultConfigTest = new VaultConfig(
                (VaultCaller) kraftwerkExecutionContext.getVaultContext().getVaultCaller(),
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