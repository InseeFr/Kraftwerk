package fr.insee.kraftwerk.api.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GenesisClient {

	private final RestTemplate restTemplate;

	@Getter
	private final ConfigProperties configProperties;


	@Autowired
	public GenesisClient(RestTemplateBuilder restTemplateBuilder, ConfigProperties configProperties) {
		this.restTemplate = restTemplateBuilder.build();
		this.configProperties = configProperties;
		OidcService oidcService = new OidcService(configProperties);
		this.restTemplate.getInterceptors().add(new GenesisAuthInterceptor(oidcService));
	}

	//Constructors used for tests
	public GenesisClient(ConfigProperties configProperties) {
		restTemplate = null;
		this.configProperties = configProperties;
	}
	public GenesisClient(RestTemplate restTemplateForTest, ConfigProperties configProperties) {
		restTemplate = restTemplateForTest;
		this.configProperties = configProperties;
	}

	private <T, R> ResponseEntity<R> makeApiCall(String url, HttpMethod method, T requestBody, Class<R> responseType) throws KraftwerkException {
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<T> requestEntity = new HttpEntity<>(requestBody, headers);

		try {
			ResponseEntity<R> response = restTemplate.exchange(url, method, requestEntity, responseType);
			if (response.getStatusCode().is2xxSuccessful()) {
				return response;
			} else {
				throw new KraftwerkException(500,"Unexpected error : " + response.getStatusCode());
			}
		} catch (RestClientResponseException e) {
			HttpStatusCode statusCode = e.getStatusCode();
            throw new KraftwerkException(500,String.format("Unabled to reach Genesis API, http code received : %d",statusCode.value()));
		}
	}

	public String pingGenesis(){
		String url = String.format("%s/health-check", configProperties.getGenesisUrl());
		// We use another restTemplate because we don't need a token to ping Genesis
		RestTemplate restTemplateWithoutAuth = new RestTemplate();
		//Null requestEntity because health check is whitelisted
		ResponseEntity<String> response = restTemplateWithoutAuth.exchange(url, HttpMethod.GET, null, String.class);
		return response.getBody() != null ? response.getBody() : null;
	}

	public List<InterrogationId> getInterrogationIds(String questionnaireId) throws KraftwerkException {
		String url = String.format("%s/interrogations/by-questionnaire?questionnaireId=%s",
				configProperties.getGenesisUrl(), questionnaireId);
		ResponseEntity<InterrogationId[]> response = makeApiCall(url,HttpMethod.GET,null,InterrogationId[].class);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}


	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 */
	public List<InterrogationId> getPaginatedInterrogationIds(String questionnaireId, long totalSize, int workersNumbers, int workerId,
															  long blockSize, long page) throws KraftwerkException {
		String url = String.format("%s/interrogations/by-questionnaire/%s/paginated?totalSize=%s&workersNumbers=%s&workerId=%s&blockSize=%s&page=%s",
				configProperties.getGenesisUrl(), questionnaireId, totalSize, workersNumbers, workerId, blockSize, page);
		ResponseEntity<InterrogationId[]> response = makeApiCall(url,HttpMethod.GET,null,InterrogationId[].class);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}


	/**
	 * @author Adrien Marchal
	 */
	public Long countInterrogationIds(String questionnaireId) throws KraftwerkException {
		String url = String.format("%s/interrogations/by-questionnaire/%s/count", configProperties.getGenesisUrl(), questionnaireId);
		ResponseEntity<Long> response = makeApiCall(url,HttpMethod.GET,null, Long.class);
		return response.getBody() != null ? response.getBody() : null;
	}


	/**
	 * @author Adrien Marchal
	 */
	public List<String> getDistinctModesByQuestionnaire(String questionnaireId) throws KraftwerkException {
		String url = String.format("%s/modes/by-questionnaire?questionnaireId=%s", configProperties.getGenesisUrl(), questionnaireId);
		ResponseEntity<String[]> response = makeApiCall(url,HttpMethod.GET,null, String[].class);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}
	//========= OPTIMISATIONS PERFS (END) ==========


	public List<Mode> getModes(String campaignId) throws KraftwerkException {
		String url = String.format("%s/modes/by-campaign?campaignId=%s", configProperties.getGenesisUrl(), campaignId);
		ResponseEntity<String[]> response = makeApiCall(url, HttpMethod.GET, null, String[].class);
		List<Mode> modes = new ArrayList<>();
		if (response.getBody() != null) Arrays.asList(response.getBody()).forEach(modeLabel -> modes.add(Mode.getEnumFromModeName(modeLabel)));
		return modes;
	}
	
	public List<SurveyUnitUpdateLatest> getUEsLatestState(String questionnaireId, List<InterrogationId> interrogationIds) throws KraftwerkException {
		String url = String.format("%s/responses/simplified/by-list-interrogation-and-questionnaire/latest?questionnaireId=%s", configProperties.getGenesisUrl(), questionnaireId);
		ResponseEntity<SurveyUnitUpdateLatest[]> response = makeApiCall(url,HttpMethod.POST,interrogationIds,SurveyUnitUpdateLatest[].class);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}

	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 */
	public List<SurveyUnitUpdateLatest> getUEsLatestStateV2(String questionnaireId, List<InterrogationId> interrogationIds, List<String> modes) throws KraftwerkException {
		//Convert Array into String
		StringBuilder sbModes = new StringBuilder();
		int index = 0;
		for(String mode : modes) {
			sbModes.append(mode);
			if(modes.size() > index + 1 ) {
				sbModes.append(",");
			}
			index++;
		}

		String url = String.format("%s/responses/simplified/by-list-interrogation-and-questionnaire/latestV2?questionnaireId=%s&modes=%s",
				configProperties.getGenesisUrl(), questionnaireId, sbModes.toString());
		ResponseEntity<SurveyUnitUpdateLatest[]> response = makeApiCall(url,HttpMethod.POST,interrogationIds,SurveyUnitUpdateLatest[].class);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}
	//========= OPTIMISATIONS PERFS (END) ==========

    public List<String> getQuestionnaireModelIds(String campaignId) throws JsonProcessingException, KraftwerkException {
		String url = String.format("%s/questionnaires/by-campaign?campaignId=%s", configProperties.getGenesisUrl(), campaignId);
		ResponseEntity<String> response = makeApiCall(url,HttpMethod.GET,null,String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		return response.getBody() != null ? objectMapper.readValue(response.getBody(), new TypeReference<>(){}) : null;
	}

}
