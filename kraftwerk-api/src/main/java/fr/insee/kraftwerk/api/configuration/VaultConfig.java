package fr.insee.kraftwerk.api.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class VaultConfig {

    @Value("${fr.insee.kraftwerk.encryption.vault.uri}")
    private String vaultUri;

    @Value("${fr.insee.trust.kraftwerk.encryption.app-role.role-id}")
    private String roleId;

    @Value("${fr.insee.trust.kraftwerk.encryption.app-role.secret-id}")
    private String secretId;

}
