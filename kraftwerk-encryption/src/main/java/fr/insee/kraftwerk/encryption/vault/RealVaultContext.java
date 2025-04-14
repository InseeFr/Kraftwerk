package fr.insee.kraftwerk.encryption.vault;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.encryption.VaultContext;
import fr.insee.libjavachiffrement.vault.VaultCaller;
import fr.insee.libjavachiffrement.vault.VaultConfig;
import lombok.Getter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Getter
@Component
@Profile("!ci-public")
public class RealVaultContext implements VaultContext { // Cannot use record because of stub subclass

    private VaultCaller vaultCaller;
    private String vaultPath;

    public RealVaultContext(VaultConfig vaultConfig) {
        if (vaultConfig != null) {
            this.vaultCaller = new VaultCaller(
                    vaultConfig.getVaultName(),
                    vaultConfig.getVaultSecretPropertyName(),
                    Constants.VAULT_APPROLE_ENDPOINT
            );
            this.vaultPath = vaultConfig.getVaultPath();
        }
    }

}
