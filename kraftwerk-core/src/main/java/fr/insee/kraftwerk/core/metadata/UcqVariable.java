package fr.insee.kraftwerk.core.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/** One unique choice question = one variable. */
@Log4j2
public class UcqVariable extends Variable {

	/**
	 * Map to store the UCQ modalities. Keys: possible values. Values: text
	 * associated.
	 */
	@Getter
	List<UcqModality> modalities = new ArrayList<>();


    /** Common name of the different modalities of the UCQ variable, if this variable is itself a modality of a Mcq variable. */
	@Getter
	@Setter
    String mcqName;
    
	public UcqVariable(String name, Group group, VariableType type) {
		super(name, group, type);
		if (type != VariableType.STRING) {
			log.warn(String.format("%s type given when creating UCQ \"%s\"", type, name));
			log.warn("Type of a UCQ variable should be STRING.");
		}
	}

	public UcqVariable(String name, Group group, VariableType type, String variableLength) {
        super(name, group, type, variableLength);
        if (type != VariableType.STRING) {
            log.warn(String.format("%s type given when creating UCQ \"%s\"", type, name));
            log.warn("Type of a UCQ variable should be STRING.");
        }
    }

	/** Add a modality using the object. */
	public void addModality(UcqModality modality) {
		modalities.add(modality);
	}

	/**
	 * Method to register a modality of the UCQ.
	 *
	 * @param value A possible value of the variable.
	 * @param text  The text associated with the value.
	 */
	public void addModality(String value, String text) {
		modalities.add(new UcqModality(value, text));
	}

	/**
	 * Method to register a modality of the UCQ.
	 *
	 * @param value        A possible value of the UCQ variable.
	 * @param text         The text associated with the value.
	 * @param variableName The name of the variable associated with the modality
	 *                     (when carrying out paper data).
	 */
	public void addModality(String value, String text, String variableName) {
		modalities.add(new UcqModality(value, text, variableName));
	}

	/** Return the possible values of the variable. */
	public Set<String> getValues() {
		return modalities.stream().map(ucqModality -> ucqModality.value).collect(Collectors.toSet());
	}

	/** Return the names of eventual variables associated with modalities. */
	public Set<String> getModalityNames() {
		return modalities.stream().map(ucqModality -> ucqModality.variableName).collect(Collectors.toSet());
	}

	/**
	 * Get a modality from its associated value.
	 *
	 * @param value One of the possible values of the variable.
	 * @return The modality object that corresponds to this value. If there is no
	 *         modality with this value, a null is returned. If several modalities
	 *         correspond to the value, the first modality that corresponds is
	 *         returned.
	 */
	public UcqModality getModalityFromValue(String value) {
		List<UcqModality> res = modalities.stream().filter(ucqModality -> ucqModality.getValue().equals(value))
				.collect(Collectors.toList());
		if (res.size() == 1) {
			return res.get(0);
		} else if (res.isEmpty()) {
			log.debug(String.format("Value \"%s\" not registered in UCQ named \"%s\".", value, name));
			return null;
		} else {
			log.debug(String.format("Several modalities with value \"%s\" are registered in UCQ named \"%s\".", value,
					name));
			return res.get(0);
		}
	}

	/**
	 * Get a modality from its name (that may be filled when carrying out paper
	 * data).
	 *
	 * @param modalityName The variable name of a modality of the UCQ.
	 * @return The modality object that corresponds to this name. If there is no
	 *         modality with this name, a null is returned. If several modalities
	 *         correspond to the name, the first modality that corresponds is
	 *         returned.
	 */
	public UcqModality getModalityFromName(String modalityName) {
		List<UcqModality> res = modalities.stream()
				.filter(ucqModality -> ucqModality.getVariableName().equals(modalityName)).collect(Collectors.toList());
		if (res.size() == 1) {
			return res.get(0);
		} else if (res.isEmpty()) {
			log.debug(String.format("Modality name \"%s\" not registered in UCQ named \"%s\".", modalityName, name));
			return null;
		} else {
			log.debug(String.format("Several modality variables named \"%s\" are registered in UCQ named \"%s\".",
					modalityName, name));
			return res.get(0);
		}
	}
}
