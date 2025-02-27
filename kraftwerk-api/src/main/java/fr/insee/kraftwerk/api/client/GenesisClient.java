package fr.insee.kraftwerk.api.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitId;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GenesisClient {

	private final RestTemplate restTemplate;

	@Getter
	private final ConfigProperties configProperties;

	private final String authToken;


	@Autowired
	public GenesisClient(RestTemplateBuilder restTemplateBuilder, ConfigProperties configProperties) {
		this.restTemplate = restTemplateBuilder.build();
		this.configProperties = configProperties;
		this.authToken = null;
	}

	public GenesisClient(RestTemplateBuilder restTemplateBuilder, ConfigProperties configProperties, String authToken) {
		this.restTemplate = restTemplateBuilder.build();
		this.configProperties = configProperties;
		this.authToken = authToken;
	}

	public String pingGenesis(){
		String url = String.format("%s/health-check", configProperties.getGenesisUrl());
		//Null requestEntity because health check is whitelisted
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
		return response.getBody() != null ? response.getBody() : null;
	}

	public List<SurveyUnitId> getSurveyUnitIds(String idQuestionnaire) {
		String url = String.format("%s/idUEs/by-questionnaire?idQuestionnaire=%s", configProperties.getGenesisUrl(), idQuestionnaire);
		ResponseEntity<SurveyUnitId[]> response = restTemplate.exchange(
				url,
				HttpMethod.GET,
				new HttpEntity<>(null, getHttpHeaders()),
				SurveyUnitId[].class
		);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}

	public List<Mode> getModes(String idCampaign) {
		String url = String.format("%s/modes/by-campaign?idCampaign=%s", configProperties.getGenesisUrl(), idCampaign);
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

	public List<SurveyUnitUpdateLatest> getUEsLatestState(String idQuestionnaire, List<SurveyUnitId> idUEs) {
		String url = String.format("%s/responses/simplified/by-list-ue-and-questionnaire/latest?idQuestionnaire=%s", configProperties.getGenesisUrl(), idQuestionnaire);
		HttpEntity<List<SurveyUnitId>> request = new HttpEntity<>(idUEs, getHttpHeaders());
		ResponseEntity<SurveyUnitUpdateLatest[]> response = restTemplate.exchange(
				url,
				HttpMethod.POST,
				request,
				SurveyUnitUpdateLatest[].class
		);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}

    public List<String> getQuestionnaireModelIds(String idCampaign) throws JsonProcessingException {
		String url = String.format("%s/questionnaires/by-campaign?idCampaign=%s", configProperties.getGenesisUrl(), idCampaign);
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
		httpHeaders.add("Authorization", "Bearer " + (authToken == null ? getTokenValue() : authToken));
		return httpHeaders;
	}

	private String getTokenValue() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		JwtAuthenticationToken oauthToken = (JwtAuthenticationToken) authentication;
		return oauthToken.getToken().getTokenValue();
	}
}
