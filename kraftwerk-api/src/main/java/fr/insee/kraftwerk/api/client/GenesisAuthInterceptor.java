package fr.insee.kraftwerk.api.client;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class GenesisAuthInterceptor implements ClientHttpRequestInterceptor {

    private final OidcService oidcService;

    public GenesisAuthInterceptor(OidcService oidcService) {
        this.oidcService = oidcService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String appToken = oidcService.getServiceAccountToken();
        request.getHeaders().set("Authorization", "Bearer " + appToken);
        return execution.execute(request, body);
    }
}
