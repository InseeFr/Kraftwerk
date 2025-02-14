package fr.insee.kraftwerk.core.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class InterrogationId {

	@JsonProperty("interrogationId")
	private String id;

}
