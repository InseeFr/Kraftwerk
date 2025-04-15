package fr.insee.kraftwerk.encryption.vault;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.encryption.VaultContext;
import fr.insee.libjavachiffrement.vault.VaultCaller;
import lombok.Getter;

@Getter
public class RealVaultContext implements VaultContext { // Cannot use record because of stub subclass

    private VaultCaller vaultCaller;
    private String vaultPath;

    public RealVaultContext(String secretId, String roleId, String vaultUri) {
        if (secretId != null && roleId!=null && vaultUri!=null) {
            this.vaultCaller = new VaultCaller(
                    roleId,
                    secretId,
                    Constants.VAULT_APPROLE_ENDPOINT
            );
            this.vaultPath = vaultUri;
        }
    }

}
