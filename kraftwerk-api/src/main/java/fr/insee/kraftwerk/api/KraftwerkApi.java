package fr.insee.kraftwerk.api;

import fr.insee.kraftwerk.api.configuration.MinioConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties(MinioConfig.class)
public class KraftwerkApi extends SpringBootServletInitializer {

	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(KraftwerkApi.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(KraftwerkApi.class, args);
	}
}
