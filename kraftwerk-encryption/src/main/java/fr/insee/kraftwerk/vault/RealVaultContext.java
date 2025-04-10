package fr.insee.kraftwerk.vault;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.encryption.VaultContext;
import fr.insee.libjavachiffrement.vault.VaultCaller;
import fr.insee.libjavachiffrement.vault.VaultConfig;
import lombok.Getter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Getter
@Profile("!ci-public")
@Component
public class RealVaultContext implements VaultContext { // Cannot use record because of stub subclass

    private final VaultCaller vaultCaller;
    private final String vaultPath;

    public RealVaultContext(VaultConfig vaultConfig) {
        this.vaultCaller = new VaultCaller(
                vaultConfig.getVaultName(),
                vaultConfig.getVaultSecretPropertyName(),
                Constants.VAULT_APPROLE_ENDPOINT
        );
        this.vaultPath =  vaultConfig.getVaultPath();
    }

}
