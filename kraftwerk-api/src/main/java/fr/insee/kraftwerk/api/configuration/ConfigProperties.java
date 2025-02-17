package fr.insee.kraftwerk.api.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ConfigProperties {

	@Value("${fr.insee.postcollecte.genesis.api.url}")
	private String genesisUrl;

	@Value("${fr.insee.postcollecte.files}")
	private String defaultDirectory;

	//Auth
	@Value("${fr.insee.kraftwerk.oidc.auth-server-url}")
	private String authServerUrl;
	@Value("${fr.insee.kraftwerk.oidc.realm}")
	private String realm;

}
