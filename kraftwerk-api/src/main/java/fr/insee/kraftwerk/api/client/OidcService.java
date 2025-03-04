package fr.insee.kraftwerk.api.client;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import lombok.Getter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
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

    private void retrieveServiceAccountToken() throws IOException {
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
        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            serviceAccountToken = (String) response.getBody().get("access_token");
            Integer expiresIn = (Integer) response.getBody().get("expires_in");
            tokenExpirationTime = System.currentTimeMillis() + (expiresIn.longValue() * 1000L);
        } else {
            throw new IOException("Failed to retrieve service account token");
        }
    }

    public String getServiceAccountToken() throws IOException {
        //We had a margin of 5 seconds for the expiration time
        if (serviceAccountToken == null || System.currentTimeMillis() >= tokenExpirationTime - 5000L) {
            retrieveServiceAccountToken();
        }
        return serviceAccountToken;
    }

}
