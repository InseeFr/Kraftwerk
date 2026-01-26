package fr.insee.kraftwerk.api.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.dto.LastJsonExtractionDate;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Slf4j
@Service
public class GenesisClient {

	private final RestClient restClient;
	private final RestClient genesisHealthRestClient;
	private final ConfigProperties configProperties;


	@Autowired
	public GenesisClient(
			@Qualifier("genesisRestClient") RestClient genesisRestClient,
			@Qualifier("genesisHealthRestClient") RestClient genesisHealthRestClient,
			ConfigProperties configProperties) {
        this.genesisHealthRestClient = genesisHealthRestClient;
        this.configProperties = configProperties;
		this.restClient = genesisRestClient;
	}

	private <T, R> ResponseEntity<R> makeApiCall(String url, HttpMethod method, T requestBody, Class<R> responseType) {
		RestClient.RequestHeadersSpec<?> requestSpec;

		String methodName = method.name();
        requestSpec = switch (methodName) {
            case "GET" -> restClient.get().uri(url);
            case "POST" -> restClient.post().uri(url).body(requestBody);
            case "PUT" -> restClient.put().uri(url).body(requestBody);
            case "DELETE" -> restClient.delete().uri(url);
            default -> throw new IllegalArgumentException("HTTP method not supported: " + method);
        };

		try{
			return requestSpec
					.retrieve()
					.toEntity(responseType);
		} catch (RestClientResponseException e) {
			HttpStatus httpStatus = HttpStatus.resolve(e.getStatusCode().value());
			// Just in case of a non standard statusCode
			if (httpStatus == null) {
				httpStatus = HttpStatus.BAD_GATEWAY;
			}
			log.error("Error reaching Genesis API, http code received: {}",e.getStatusCode().value());
			throw new GenesisApiException(
					httpStatus,
					String.format("Error reaching Genesis API, http code received: %d", e.getStatusCode().value()),
					e);
		} catch (RestClientException e) {
			log.error("Genesis API unreachable: {}",e.getMessage());
			throw new GenesisApiException(
					HttpStatus.SERVICE_UNAVAILABLE,
					"Genesis API unreachable",
					e);
		} catch (Exception e) {
			log.error("Unexpected error calling Genesis API: {}",e.getMessage());
			throw new GenesisApiException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Unexpected error calling Genesis API",
					e);
		}
	}

	public String pingGenesis(){
		return genesisHealthRestClient.get()
				.uri("/health-check")
				.retrieve()
				.body(String.class);
	}

	public List<InterrogationId> getInterrogationIds(String questionnaireId) throws KraftwerkException {
		String url = String.format("%s/interrogations/by-questionnaire?questionnaireId=%s",
				configProperties.getGenesisUrl(), questionnaireId);
		ResponseEntity<InterrogationId[]> response = makeApiCall(url, GET,null,InterrogationId[].class);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}

	public List<InterrogationId> getInterrogationIdsFromDate(String questionnaireId, LocalDateTime since) throws KraftwerkException {
		String url = String.format("%s/interrogations/by-questionnaire-and-since-datetime?questionnaireId=%s&since=%s",
				configProperties.getGenesisUrl(), questionnaireId,since);
		ResponseEntity<InterrogationId[]> response = makeApiCall(url, GET,null,InterrogationId[].class);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}

	public List<Mode> getModes(String campaignId) throws KraftwerkException {
		String url = String.format("%s/modes/by-campaign?campaignId=%s", configProperties.getGenesisUrl(), campaignId);
		ResponseEntity<String[]> response = makeApiCall(url, GET, null, String[].class);
		List<Mode> modes = new ArrayList<>();
		if (response.getBody() != null) Arrays.asList(response.getBody()).forEach(modeLabel -> modes.add(Mode.getEnumFromModeName(modeLabel)));
		return modes;
	}

	public List<Mode> getModesByQuestionnaire(String questionnaireModelId) throws KraftwerkException {
		String url = String.format("%s/modes/by-questionnaire?questionnaireId=%s", configProperties.getGenesisUrl(), questionnaireModelId);
		ResponseEntity<String[]> response = makeApiCall(url, GET, null, String[].class);
		List<Mode> modes = new ArrayList<>();
		if (response.getBody() != null) Arrays.asList(response.getBody()).forEach(modeLabel -> modes.add(Mode.getEnumFromModeName(modeLabel)));
		return modes;
	}
	
	public List<SurveyUnitUpdateLatest> getUEsLatestState(String questionnaireId, List<InterrogationId> interrogationIds) throws KraftwerkException {
		String url = String.format("%s/responses/simplified/by-list-interrogation-and-collection-instrument/latest?collectionInstrumentId=%s", configProperties.getGenesisUrl(), questionnaireId);
		ResponseEntity<SurveyUnitUpdateLatest[]> response = makeApiCall(url, POST,interrogationIds,SurveyUnitUpdateLatest[].class);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}

    public List<String> getQuestionnaireModelIds(String campaignId) throws JsonProcessingException, KraftwerkException {
		String url = String.format("%s/questionnaires/by-campaign?campaignId=%s", configProperties.getGenesisUrl(), campaignId);
		ResponseEntity<String> response = makeApiCall(url, GET,null,String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		return response.getBody() != null ? objectMapper.readValue(response.getBody(), new TypeReference<>(){}) : null;
	}

	public String getQuestionnaireModelIdByInterrogationId(String interrogationId) throws KraftwerkException {
		String url = String.format("%s/questionnaires/by-interrogation?interrogationId=%s", configProperties.getGenesisUrl(), interrogationId);
		ResponseEntity<String> response = makeApiCall(url, GET,null,String.class);
		return response.getBody() != null ? response.getBody() : null;
	}

	public MetadataModel getMetadataByQuestionnaireIdAndMode(String questionnaireId, Mode mode) throws KraftwerkException {
		String url = String.format("%s/questionnaire-metadata?questionnaireId=%s&mode=%s",
				configProperties.getGenesisUrl(), questionnaireId, mode);
		ResponseEntity<MetadataModel> response = makeApiCall(url, GET,null,MetadataModel.class);
		return response.getBody();
    }

	public void saveMetadata(String questionnaireId, Mode mode, MetadataModel metadataModel) throws KraftwerkException {
		String url = String.format("%s/questionnaire-metadata?questionnaireId=%s&mode=%s",
				configProperties.getGenesisUrl(), questionnaireId, mode);
		makeApiCall(url, POST,metadataModel,null);
	}

	public void saveDateExtraction(String questionnaireModelId, Mode mode) throws KraftwerkException {
		String url = String.format("%s/extractions/json?collectionInstrumentId=%s",
				configProperties.getGenesisUrl(), questionnaireModelId);
		if (mode != null) {
			url += "&mode=" + mode;
		}
		makeApiCall(url, PUT,null,null);
	}

	public LastJsonExtractionDate getLastExtractionDate(String questionnaireModelId, Mode mode) throws KraftwerkException {
		String url = String.format("%s/extractions/json?collectionInstrumentId=%s",
				configProperties.getGenesisUrl(), questionnaireModelId);
		if (mode != null) {
			url += "&mode=" + mode;
		}
		ResponseEntity<LastJsonExtractionDate> response = makeApiCall(url, GET,null,LastJsonExtractionDate.class);
		return response.getBody();
	}
}
