package fr.insee.kraftwerk.api.configuration.rest;

import fr.insee.kraftwerk.api.client.GenesisAuthInterceptor;
import fr.insee.kraftwerk.api.client.OidcService;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient genesisRestClient(RestClient.Builder builder, ConfigProperties configProperties) {
        OidcService oidcService = new OidcService(configProperties);
        return builder
                .baseUrl(configProperties.getGenesisUrl())
                .requestInterceptor(new GenesisAuthInterceptor(oidcService))
                .build();
    }

    @Bean
    public RestClient genesisHealthRestClient(RestClient.Builder builder, ConfigProperties configProperties) {
        return builder
                .baseUrl(configProperties.getGenesisUrl())
                .build();
    }

}
