package fr.insee.kraftwerk.encryption.utils;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.libjavachiffrement.vault.VaultCaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test-cucumber")
public class VaultCallerImpl extends VaultCaller {

    @Autowired
    public VaultCallerImpl(@Value("${fr.insee.kraftwerk.encryption.vault.app-role.role-id}") String roleId,
                           @Value("${fr.insee.kraftwerk.encryption.vault.app-role.secret-id}") String secretId) {
        super(roleId, secretId, Constants.VAULT_APPROLE_ENDPOINT);
    }
}
