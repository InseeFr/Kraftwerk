package fr.insee.kraftwerk.api.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class GenesisClient {

	private final RestTemplate restTemplate;

	@Getter
	private final ConfigProperties configProperties;

	private String serviceAccountToken;


	@Autowired
	public GenesisClient(RestTemplateBuilder restTemplateBuilder, ConfigProperties configProperties) {
		this.restTemplate = restTemplateBuilder.build();
		this.configProperties = configProperties;
		this.serviceAccountToken = retrieveServiceAccountToken();
	}

	public GenesisClient(RestTemplateBuilder restTemplateBuilder, ConfigProperties configProperties, String authToken) {
		this.restTemplate = restTemplateBuilder.build();
		this.configProperties = configProperties;
		this.serviceAccountToken = retrieveServiceAccountToken();
	}

	//Constructor used for tests
	public GenesisClient(ConfigProperties configProperties) {
		restTemplate = null;
		this.configProperties = configProperties;
		this.serviceAccountToken = retrieveServiceAccountToken();
	}

	private synchronized String retrieveServiceAccountToken() {
		String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
				configProperties.getKeycloakUrl(),
				configProperties.getKeycloakRealm());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		String body = String.format("grant_type=client_credentials&client_id=%s&client_secret=%s",
				configProperties.getServiceClientId(),
				configProperties.getServiceClientSecret());

		HttpEntity<String> request = new HttpEntity<>(body, headers);

		ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
		if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
			return (String) response.getBody().get("access_token");
		} else {
			throw new RuntimeException("Failed to retrieve service account token");
		}
	}


	@Scheduled(fixedDelayString = "${service.token.refresh.interval:300000}")
	public synchronized void refreshServiceAccountToken() {
		this.serviceAccountToken = retrieveServiceAccountToken();
	}

	public String pingGenesis(){
		String url = String.format("%s/health-check", configProperties.getGenesisUrl());
		//Null requestEntity because health check is whitelisted
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
		return response.getBody() != null ? response.getBody() : null;
	}

	public List<InterrogationId> getInterrogationIds(String questionnaireId) {
		String url = String.format("%s/interrogations/by-questionnaire?questionnaireId=%s",
				configProperties.getGenesisUrl(), questionnaireId);
		ResponseEntity<InterrogationId[]> response = restTemplate.exchange(
				url,
				HttpMethod.GET,
				new HttpEntity<>(null, getHttpHeaders()),
				InterrogationId[].class
		);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}

	public List<Mode> getModes(String campaignId) {
		String url = String.format("%s/modes/by-campaign?campaignId=%s", configProperties.getGenesisUrl(), campaignId);
		ResponseEntity<String[]> response = restTemplate.exchange(
				url,
				HttpMethod.GET,
				new HttpEntity<>(null, getHttpHeaders()),
				String[].class
		);
		List<Mode> modes = new ArrayList<>();
		if (response.getBody() != null) Arrays.asList(response.getBody()).forEach(modeLabel -> modes.add(Mode.getEnumFromModeName(modeLabel)));
		return modes;
	}
	
	public List<SurveyUnitUpdateLatest> getUEsLatestState(String questionnaireId, List<InterrogationId> interrogationIds) {
		String url = String.format("%s/responses/simplified/by-list-interrogation-and-questionnaire/latest?questionnaireId=%s", configProperties.getGenesisUrl(), questionnaireId);
		HttpEntity<List<InterrogationId>> request = new HttpEntity<>(interrogationIds, getHttpHeaders());
		ResponseEntity<SurveyUnitUpdateLatest[]> response = restTemplate.exchange(
				url,
				HttpMethod.POST,
				request,
				SurveyUnitUpdateLatest[].class
		);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}

    public List<String> getQuestionnaireModelIds(String campaignId) throws JsonProcessingException {
		String url = String.format("%s/questionnaires/by-campaign?campaignId=%s", configProperties.getGenesisUrl(), campaignId);
		ResponseEntity<String> response = restTemplate.exchange(url,
				HttpMethod.GET,
				new HttpEntity<>(null, getHttpHeaders()),
				String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		return response.getBody() != null ? objectMapper.readValue(response.getBody(), new TypeReference<>(){}) : null;
	}

	private HttpHeaders getHttpHeaders() {
		//Auth
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Bearer " + serviceAccountToken;
		return httpHeaders;
	}

	public void validateUserToken() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof JwtAuthenticationToken)) {
			throw new RuntimeException("Invalid authentication type");
		}
	}
}
