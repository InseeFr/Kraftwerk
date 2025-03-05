package fr.insee.kraftwerk.api.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Slf4j
public class GenesisAuthInterceptor implements ClientHttpRequestInterceptor {

    private final OidcService oidcService;

    public GenesisAuthInterceptor(OidcService oidcService) {
        this.oidcService = oidcService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String appToken = oidcService.getServiceAccountToken();
        request.getHeaders().set("Authorization", "Bearer " + appToken);

        ClientHttpResponse response = execution.execute(request, body);

        // If the request return a 401, we try to refresh the token and we retry one time
        if (response.getStatusCode().value() == 401) {
            log.warn("Received a response 401, we try one more time with a new token");
            oidcService.retrieveServiceAccountToken(); // Force token refresh
            request.getHeaders().set("Authorization", "Bearer " + oidcService.getServiceAccountToken());
            response = execution.execute(request, body);
        }

        return response;
    }
}
