package fr.insee.kraftwerk.api.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Value("${fr.insee.kraftwerk.version}")
    private String projectVersion;

    public static final String BEARERSCHEME = "bearerAuth";

    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kraftwerk API")
                        .description("Rest Endpoints and services exposed by Kraftwerk")
                        .version(projectVersion)
                );
    }

    @Bean
    @ConditionalOnProperty(name = "fr.insee.kraftwerk.authentication", havingValue = "NONE")
    public OpenAPI noAuthOpenAPI() {
        return customOpenAPI();
    }

    @Bean
    @ConditionalOnProperty(name = "fr.insee.kraftwerk.authentication", havingValue = "OIDC")
    public OpenAPI oidcOpenAPI() {
        return customOpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(BEARERSCHEME))
                .components(
                        new Components()
                                .addSecuritySchemes(BEARERSCHEME,
                                        new SecurityScheme()
                                                .name(BEARERSCHEME)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}
