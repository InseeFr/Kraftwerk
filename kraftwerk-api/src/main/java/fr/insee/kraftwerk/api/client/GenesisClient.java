package fr.insee.kraftwerk.api.client;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.inputs.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GenesisClient {

	private final RestTemplate restTemplate;

	private final ConfigProperties configProperties;


	@Autowired
	public GenesisClient(RestTemplateBuilder restTemplateBuilder, ConfigProperties configProperties) {
		this.restTemplate = restTemplateBuilder.build();
		this.configProperties = configProperties;
	}

	public List<SurveyUnitId> getSurveyUnitIds(String idQuestionnaire) {
		String url = String.format("%s/response/get-idUEs/by-questionnaire?idQuestionnaire=%s", configProperties.getGenesisUrl(), idQuestionnaire);
		ResponseEntity<SurveyUnitId[]> response = restTemplate.exchange(url, HttpMethod.GET, null, SurveyUnitId[].class);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}

	public List<Mode> getModes(String idQuestionnaire) {
		String url = String.format("%s/response/get-modes/by-questionnaire?idQuestionnaire=%s", configProperties.getGenesisUrl(), idQuestionnaire);
		ResponseEntity<String[]> response = restTemplate.exchange(url, HttpMethod.GET, null, String[].class);
		List<Mode> modes = new ArrayList<>();
		if (response.getBody() != null) Arrays.asList(response.getBody()).forEach(modeLabel -> modes.add(Mode.getEnumFromModeName(modeLabel)));
		return modes;
	}

	public List<SurveyUnitUpdateLatest> getUELatestState(String idQuestionnaire, SurveyUnitId suId) {
		String url = String.format("%s/response/get-simplified-response/by-ue-and-questionnaire/latest?idQuestionnaire=%s&idUE=%s", configProperties.getGenesisUrl(), idQuestionnaire, suId.getIdUE());
		ResponseEntity<SurveyUnitUpdateLatest[]> response = restTemplate.exchange(url, HttpMethod.GET, null, SurveyUnitUpdateLatest[].class);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}

	public List<SurveyUnitUpdateLatest> getUEsLatestState(String idQuestionnaire, List<SurveyUnitId> idUEs) {
		String url = String.format("%s/response/get-simplified-responses/by-ue-and-questionnaire/latest?idQuestionnaire=%s", configProperties.getGenesisUrl(), idQuestionnaire);
		HttpEntity<List<SurveyUnitId>> request = new HttpEntity<>(idUEs);
		ResponseEntity<SurveyUnitUpdateLatest[]> response = restTemplate.exchange(url, HttpMethod.POST, request, SurveyUnitUpdateLatest[].class);
		return response.getBody() != null ? Arrays.asList(response.getBody()) : null;
	}

}
