package fr.insee.kraftwerk;

import fr.insee.kraftwerk.api.configuration.MinioConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.Arrays;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties(MinioConfig.class)
@Slf4j
public class KraftwerkApi extends SpringBootServletInitializer {

	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(KraftwerkApi.class);
	}

	public static void main(String[] args) {
		boolean hasProfileArg = Arrays.stream(args)
				.anyMatch(arg -> arg.startsWith("--spring.profiles.active="));

		SpringApplication app = new SpringApplication(KraftwerkApi.class);

		if (!hasProfileArg) {
			app.setAdditionalProfiles("dev"); // default fallback
			log.info("🔧 No profile provided. Falling back to 'dev'");
		}else{
			log.info("🔧 Profile explicitly set via args, no override.");
		}

		app.run(args);
	}
}
