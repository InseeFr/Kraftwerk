package fr.insee.kraftwerk.api.configuration;


import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
@Getter
public class ConfigProperties {

	@Value("${fr.insee.postcollecte.genesis.api.url}")
	private String genesisUrl;

	@Value("${fr.insee.postcollecte.files}")
	private String defaultDirectory;

	@PostConstruct
	public void setTempDirectory() {
		System.setProperty("java.io.tmpdir", Paths.get(defaultDirectory,"temp","currentExecution").toString());
	}

}
