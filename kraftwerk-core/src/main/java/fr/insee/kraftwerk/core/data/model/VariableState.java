package fr.insee.kraftwerk.core.data.model;

import lombok.Data;

import java.util.List;

@Data
public class VariableState {

	private String idVar;
	private String idLoop;
	private String idParent;
	private List<String> values;
}
