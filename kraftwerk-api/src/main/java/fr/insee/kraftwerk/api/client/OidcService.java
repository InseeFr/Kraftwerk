package fr.insee.kraftwerk.api.client;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class OidcService {

    public static final String ACCESS_TOKEN = "access_token";
    @Getter
    private final ConfigProperties configProperties;
    private String serviceAccountToken;
    private long tokenExpirationTime = 0;
    private final RestTemplate restTemplate;

    public OidcService(ConfigProperties configProperties) {
        this.restTemplate = new RestTemplate();
        this.configProperties = configProperties;
    }

    protected void retrieveServiceAccountToken() throws IOException {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                configProperties.getAuthServerUrl(),
                configProperties.getRealm());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = String.format("grant_type=client_credentials&client_id=%s&client_secret=%s&scope=openid profile roles",
                configProperties.getServiceClientId(),
                configProperties.getServiceClientSecret());

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                if (responseBody == null || !responseBody.containsKey(ACCESS_TOKEN) || !(responseBody.get(ACCESS_TOKEN) instanceof String)) {
                    throw new IOException("Invalid response: Missing or incorrect 'access_token'");
                }

                serviceAccountToken = (String) responseBody.get(ACCESS_TOKEN);
                Integer expiresIn = (Integer) responseBody.get("expires_in");
                tokenExpirationTime = System.currentTimeMillis() + (expiresIn.longValue() * 1000L);
            } else {
                throw new IOException("Failed to retrieve service account token, status: " + response.getStatusCode());
            }
        } catch (HttpStatusCodeException e) {
            throw new IOException("HTTP error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new IOException("Unexpected error while fetching token: " + e.getMessage(), e);
        }
    }

    public synchronized String getServiceAccountToken() throws IOException {
        //We had a margin of 5 seconds for the expiration time
        if (serviceAccountToken == null || System.currentTimeMillis() >= tokenExpirationTime - 5000L) {
            retrieveServiceAccountToken();
        }
        return serviceAccountToken;
    }

}
