package fr.insee.kraftwerk.core.data.model;

import lombok.Data;

import java.util.List;

@Data
public class VariableModel {

	private String idVar;
	private String idLoop;
	private String idParent;
	private String value;
}
