package fr.insee.kraftwerk.api.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.dto.InterrogationBatchResponse;
import fr.insee.kraftwerk.api.dto.LastJsonExtractionDate;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
            throw new KraftwerkException(500,String.format("Unable to reach Genesis API, http code received : %d",statusCode.value()));
		}
	}

	public String pingGenesis(){
		String url = String.format("%s/health-check", configProperties.getGenesisUrl());
		// We use another restTemplate because we don't need a token to ping Genesis
		RestTemplate restTemplateWithoutAuth = new RestTemplate();
		// Null requestEntity because health check is whitelisted
		ResponseEntity<String> response = restTemplateWithoutAuth.exchange(url, HttpMethod.GET, null, String.class);
		return response.getBody() != null ? response.getBody() : null;
	}

	public List<InterrogationId> getInterrogationIds(String questionnaireId) throws KraftwerkException {
		String url = String.format("%s/interrogations/by-questionnaire?questionnaireId=%s",
				configProperties.getGenesisUrl(), questionnaireId);
		ResponseEntity<InterrogationId[]> response = makeApiCall(url,HttpMethod.GET,null,InterrogationId[].class);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}

	public InterrogationBatchResponse getInterrogationBatchAll(String collectionInstrumentId) throws KraftwerkException {
		String url = String.format("%s/collection-instruments/%s/interrogations/all",
				configProperties.getGenesisUrl(), collectionInstrumentId);
		ResponseEntity<InterrogationBatchResponse> response = makeApiCall(url,HttpMethod.GET,null,InterrogationBatchResponse.class);
		return Optional.ofNullable(response.getBody()).orElseThrow(() -> new KraftwerkException(502,"Empty response body from Genesis API"));
	}

	public InterrogationBatchResponse getInterrogationBatchSince(String collectionInstrumentId, Instant since) throws KraftwerkException {
		String url = String.format("%s/collection-instruments/%s/interrogations?since=%s",
				configProperties.getGenesisUrl(), collectionInstrumentId, since);
		ResponseEntity<InterrogationBatchResponse> response = makeApiCall(url,HttpMethod.GET,null,InterrogationBatchResponse.class);
		return Optional.ofNullable(response.getBody()).orElseThrow(() -> new KraftwerkException(502,"Empty response body from Genesis API"));
	}

    public List<InterrogationId> getInterrogationIdsBetweenDates(String collectionInstrumentId, LocalDateTime start, LocalDateTime end) throws KraftwerkException {
		String url = String.format("%s/interrogations/by-collection-instrument-and-between-datetime?collectionInstrumentId=%s&start=%s&end=%s",
				configProperties.getGenesisUrl(), collectionInstrumentId,start,end);
		ResponseEntity<InterrogationId[]> response = makeApiCall(url,HttpMethod.GET,null,InterrogationId[].class);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}

	public List<Mode> getModes(String campaignId) throws KraftwerkException {
		String url = String.format("%s/modes/by-campaign?campaignId=%s", configProperties.getGenesisUrl(), campaignId);
		ResponseEntity<String[]> response = makeApiCall(url, HttpMethod.GET, null, String[].class);
		List<Mode> modes = new ArrayList<>();
		if (response.getBody() != null) Arrays.asList(response.getBody()).forEach(modeLabel -> modes.add(Mode.getEnumFromModeName(modeLabel)));
		return modes;
	}

	public List<Mode> getModesByQuestionnaire(String collectionInstrumentId) throws KraftwerkException {
		String url = String.format("%s/modes/by-questionnaire?collectionInstrumentId=%s", configProperties.getGenesisUrl(), collectionInstrumentId);
		ResponseEntity<String[]> response = makeApiCall(url, HttpMethod.GET, null, String[].class);
		List<Mode> modes = new ArrayList<>();
		if (response.getBody() != null) Arrays.asList(response.getBody()).forEach(modeLabel -> modes.add(Mode.getEnumFromModeName(modeLabel)));
		return modes;
	}
	
	public List<SurveyUnitUpdateLatest> getResponses(String collectionInstrumentId, List<InterrogationId> interrogationIds, Instant recordedBefore) throws KraftwerkException {
		String url = String.format("%s/responses/%s", configProperties.getGenesisUrl(), collectionInstrumentId);
		if (recordedBefore != null) {
			url += "?recordedBefore=" + recordedBefore;
		}
		ResponseEntity<SurveyUnitUpdateLatest[]> response = makeApiCall(url,HttpMethod.POST,interrogationIds,SurveyUnitUpdateLatest[].class);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}

    public List<String> getQuestionnaireModelIds(String campaignId) throws JsonProcessingException, KraftwerkException {
		String url = String.format("%s/questionnaires/by-campaign?campaignId=%s", configProperties.getGenesisUrl(), campaignId);
		ResponseEntity<String> response = makeApiCall(url,HttpMethod.GET,null,String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		return response.getBody() != null ? objectMapper.readValue(response.getBody(), new TypeReference<>(){}) : null;
	}

	public String getQuestionnaireModelIdByInterrogationId(String interrogationId) throws KraftwerkException {
		String url = String.format("%s/questionnaires/by-interrogation?interrogationId=%s", configProperties.getGenesisUrl(), interrogationId);
		ResponseEntity<String> response = makeApiCall(url,HttpMethod.GET,null,String.class);
		return response.getBody() != null ? response.getBody() : null;
	}

	public MetadataModel getMetadataByQuestionnaireIdAndMode(String questionnaireId, Mode mode) throws KraftwerkException {
		String url = String.format("%s/questionnaire-metadata?questionnaireId=%s&mode=%s",
				configProperties.getGenesisUrl(), questionnaireId, mode);
		ResponseEntity<MetadataModel> response = makeApiCall(url,HttpMethod.GET,null,MetadataModel.class);
		return response.getBody();
    }

	public void saveMetadata(String questionnaireId, Mode mode, MetadataModel metadataModel) throws KraftwerkException {
		String url = String.format("%s/questionnaire-metadata?questionnaireId=%s&mode=%s",
				configProperties.getGenesisUrl(), questionnaireId, mode);
		makeApiCall(url,HttpMethod.POST,metadataModel,null);
	}

	public void saveDateExtraction(String collectionInstrumentId, Mode mode, LastJsonExtractionDate lastJsonExtractionDate) throws KraftwerkException {
		String url = String.format("%s/collection-instruments/%s/extractions/json/last",
				configProperties.getGenesisUrl(), collectionInstrumentId);
		if (mode != null) {
			url += "?mode=" + mode;
		}
		makeApiCall(url,HttpMethod.PUT,lastJsonExtractionDate,null);
	}

	public LastJsonExtractionDate getLastExtractionDate(String collectionInstrumentId, Mode mode) throws KraftwerkException {
		String url = String.format("%s/collection-instruments/%s/extractions/json/last",
				configProperties.getGenesisUrl(), collectionInstrumentId);
		if (mode != null) {
			url += "?mode=" + mode;
		}
		ResponseEntity<LastJsonExtractionDate> response = makeApiCall(url,HttpMethod.GET,null,LastJsonExtractionDate.class);
		return response.getBody();
	}
}
