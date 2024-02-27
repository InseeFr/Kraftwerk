package fr.insee.kraftwerk.core.metadata;

import lombok.Getter;
import lombok.Setter;

/**
 * POJO class to store information about a UQC modality.
 */
@Getter
public class UcqModality {

    /**
     * Value associated to the modality in survey data.
     */
    @Setter
    String value;
    /**
     * Text associated to the modality
     */
    @Setter
    String text;
    /**
     * If an indicator variable is associated to the modality (in a paper data
     * files).
     */
    @Setter
    String variableName;

    UcqModality() {
    }

    UcqModality(String value, String text) {
        this.value = value;
        this.text = text;
    }

    UcqModality(String value, String text, String variableName) {
        this.value = value;
        this.text = text;
        this.variableName = variableName;
    }
}
