package fr.insee.kraftwerk.api.services;


import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Paths;


@RestController
@Tag(name = "${tag.health-check}")
public class HealthcheckService extends KraftwerkService {

	public static final String OK = "OK";
	public static final String KO = "FAILURE";


	@Value("${fr.insee.kraftwerk.version}")
	private String projectVersion;

	private final GenesisClient client;

	ConfigProperties configProperties;

	@Autowired
	public HealthcheckService(ConfigProperties configProperties) {
		this.configProperties = configProperties;
		this.client = new GenesisClient(new RestTemplateBuilder(), configProperties);

	}


	@GetMapping("health-check")
	public ResponseEntity<String> healthcheck() {
		String status = OK;
		String fileStorageStatus = fileStorageExists();
		if (!OK.equals(fileStorageStatus)){status = KO;}

		return ResponseEntity.ok(
				"""
                             %s
                             
                             Version %s
                             Genesis health-check  %s
                             File storage %s : %s
                        """
						.formatted(
								status,
								projectVersion,
								client.pingGenesis().split("\n")[0],
								configProperties.getSpecDirectory(),
								fileStorageStatus
						));
	}

	private String fileStorageExists() {
		try{
			Files.exists(Paths.get(configProperties.getSpecDirectory()));
		}catch (Exception e){
			return "Disconnected " +e.getMessage();
		}
		return OK;
	}


}