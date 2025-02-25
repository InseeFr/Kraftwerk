package fr.insee.kraftwerk.api.client;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import lombok.Getter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OidcService {

    @Getter
    private final ConfigProperties configProperties;
    private String serviceAccountToken;
    private long tokenExpirationTime = 0;

    public OidcService(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    private String retrieveServiceAccountToken() {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                configProperties.getAuthServerUrl(),
                configProperties.getRealm());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = String.format("grant_type=client_credentials&client_id=%s&client_secret=%s",
                configProperties.getServiceClientId(),
                configProperties.getServiceClientSecret());

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        } else {
            throw new RuntimeException("Failed to retrieve service account token");
        }
    }

    public String getServiceAccountToken() {
        if (serviceAccountToken == null || System.currentTimeMillis() >= tokenExpirationTime) {
            retrieveServiceAccountToken();
        }
        return serviceAccountToken;
    }

}
