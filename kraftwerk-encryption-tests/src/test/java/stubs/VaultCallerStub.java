package stubs;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.libjavachiffrement.vault.VaultCaller;
import lombok.Getter;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class VaultCallerStub extends VaultCaller {
    private final Map<String, byte[]> vaultStub;

    public VaultCallerStub() {
        super(null, null, null);
        this.vaultStub = new LinkedHashMap<>();

        try{
            //Generate AES Key
            int keySize = 256;
            KeyGenerator keyGen = KeyGenerator.getInstance("AES"); // Use key generator from JCA
            keyGen.init(keySize);
            SecretKey secretKey = keyGen.generateKey();
            byte[] aesKeyBytes = secretKey.getEncoded();
            vaultStub.put(String.format(Constants.STRING_FORMAT_VAULT_PATH,
                    Constants.TRUST_VAULT_PATH,
                    Constants.TRUST_AES_KEY_VAULT_PATH), aesKeyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getSecret(String vaultPath, String vaultName, String secretName, String dataName){
        return vaultStub.get(secretName);
    }
}
