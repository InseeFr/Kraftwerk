package fr.insee.kraftwerk.core.metadata;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * One multiple choice question with K modalities = K MCQ Variables.
 */
@Log4j2
public class McqVariable extends Variable {

	/** Text associated with the modality. */
	@Getter
	@Setter
	String text;

	public McqVariable(String name, Group group, VariableType type) {
		super(name, group, type);
		if (type != VariableType.BOOLEAN) {
			log.warn(String.format("%s type given when creating MCQ \"%s\"", type, name));
			log.warn("Type of a MCQ variable should be BOOLEAN.");
		}
	}

	public McqVariable(String name, Group group, VariableType type, String variableLength) {
		super(name, group, type, variableLength);
		if (type != VariableType.BOOLEAN) {
			log.warn(String.format("%s type given when creating MCQ \"%s\"", type, name));
			log.warn("Type of a MCQ variable should be BOOLEAN.");
		}
	}

	/** Builder. Variable type is BOOLEAN. */
	@Builder
	public McqVariable(String name, Group group, String questionItemName, String text) {
		super(name, group, VariableType.BOOLEAN);
		this.questionItemName = questionItemName;
		this.text = text;
	}
}
