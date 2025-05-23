package cucumber;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.libjavachiffrement.config.CipherConfig;
import fr.insee.libjavachiffrement.symmetric.SymmetricEncryptionEndpoint;
import fr.insee.libjavachiffrement.symmetric.SymmetricKeyContext;
import fr.insee.libjavachiffrement.vault.VaultCaller;
import fr.insee.libjavachiffrement.vault.VaultConfig;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * Class to create static variables giving path for test resources.
 */
@Slf4j
public class TestConstants {

    public static final String TEST_RESOURCES_DIRECTORY = "src/test/resources";

    public static final String FUNCTIONAL_TESTS_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests";
    public static final String FUNCTIONAL_TESTS_INPUT_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests/in";
    public static final String FUNCTIONAL_TESTS_OUTPUT_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests/out";
    public static final String FUNCTIONAL_TESTS_TEMP_DIRECTORY = TEST_RESOURCES_DIRECTORY + "/functional_tests/temp";


    private static final String VAULT_NAME = "filiere_enquetes";
    private static final String VAULT_PROPERTY_NAME = "value";

    @NotNull
    public static KraftwerkExecutionContext getKraftwerkExecutionContext() {
        return new KraftwerkExecutionContext(
                null,
                false,
        true,
                false,
                419430400L
        );
    }

    @NotNull
    public static KraftwerkExecutionContext getKraftwerkExecutionContext(String inDirectory, boolean withEncryption) {
        return new KraftwerkExecutionContext(
                inDirectory,
                false,
                true,
                withEncryption,
                419430400L
        );
    }


    public static @NotNull SymmetricEncryptionEndpoint getSymmetricEncryptionEndpointForTest(VaultCaller vaultCaller) {
        VaultConfig vaultConfigTest = new VaultConfig(
                vaultCaller,
                Constants.TRUST_VAULT_PATH,
                VAULT_NAME,
                VAULT_PROPERTY_NAME);
        CipherConfig cipherConfig = new CipherConfig(null, null, vaultConfigTest);
        SymmetricKeyContext keyContext = new SymmetricKeyContext( String.format(Constants.STRING_FORMAT_VAULT_PATH,
                Constants.TRUST_VAULT_PATH,
                Constants.TRUST_AES_KEY_VAULT_PATH));

        return new SymmetricEncryptionEndpoint(keyContext,cipherConfig);
    }
}