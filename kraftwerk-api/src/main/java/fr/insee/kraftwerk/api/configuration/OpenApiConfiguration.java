package fr.insee.kraftwerk.api.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
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
    public static final String OAUTH2SCHEME = "oauth2";

    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kraftwerk API")
                        .description("Rest Endpoints and services exposed by Kraftwerk")
                        .version(projectVersion)
                );
    }

    @Bean
    @ConditionalOnProperty(name = "fr.insee.kraftwerk.security.authentication", havingValue = "NONE")
    public OpenAPI noAuthOpenAPI() {
        return customOpenAPI();
    }

    @Bean
    @ConditionalOnProperty(name = "fr.insee.kraftwerk.security.authentication", havingValue = "OIDC")
    public OpenAPI oidcOpenAPI(ConfigProperties config) {
        String authUrl = config.getAuthServerUrl() + "/realms/" + config.getRealm() + "/protocol/openid-connect";
        return customOpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(OAUTH2SCHEME))
                .addSecurityItem(new SecurityRequirement().addList(BEARERSCHEME))
                .components(
                        new Components()
                                .addSecuritySchemes(OAUTH2SCHEME,
                                        new SecurityScheme()
                                                .name(OAUTH2SCHEME)
                                                .type(SecurityScheme.Type.OAUTH2)
                                                .flows(getFlows(authUrl))
                                )
                                .addSecuritySchemes(BEARERSCHEME,
                                        new SecurityScheme()
                                                .name(BEARERSCHEME)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }

    private OAuthFlows getFlows(String authUrl) {
        OAuthFlows flows = new OAuthFlows();
        OAuthFlow flow = new OAuthFlow();
        Scopes scopes = new Scopes();
        flow.setAuthorizationUrl(authUrl + "/auth");
        flow.setTokenUrl(authUrl + "/token");
        flow.setRefreshUrl(authUrl + "/token");
        flow.setScopes(scopes);
        return flows.authorizationCode(flow);
    }
}
