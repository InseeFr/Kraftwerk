package fr.insee.kraftwerk.core.data.model;

import lombok.Data;

@Data
public class VariableModel {

	private String varId;
	private String scope;
	private int iteration;
	private String idParent;
	private String value;
}
