package fr.insee.kraftwerk.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Value("${fr.insee.kraftwerk.version}")
    private String projectVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kraftwerk API")
                        .description("Rest Endpoints and services exposed by Kraftwerk")
                        .version(projectVersion)
                );
    }

}
