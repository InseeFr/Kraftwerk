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
	private String specDirectory;

}
