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
	@Value("${fr.insee.kraftwerk.security.token.oidc-claim-role}")
	private String oidcClaimRole;
	@Value("${fr.insee.kraftwerk.security.token.oidc-claim-username}")
	private String oidcClaimUsername;
	@Value("#{'${fr.insee.kraftwerk.security.whitelist-matchers}'.split(',')}")
	private String[] whiteList;

	@Value("${fr.insee.kraftwerk.oidc.service.client-id}")
	private String serviceClientId;
	@Value("${fr.insee.kraftwerk.oidc.service.client-secret}")
	private String serviceClientSecret;
}
