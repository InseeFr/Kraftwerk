package fr.insee.kraftwerk.core.extradata.paradata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Session {

	@Getter@Setter
	private String identifier;

	@Getter@Setter
	private long initialization;

	@Getter@Setter
	private long termination;


}
