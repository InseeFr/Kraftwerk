package fr.insee.kraftwerk.metadata;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * One multiple choice question with K modalities = K MCQ Variables.
 * */
@Slf4j
public class McqVariable extends Variable {

    /** Common name of the different modalities of a MCQ. */
    @Getter @Setter
    String mqcName;
    /** Text associated with the modality. */
    @Getter @Setter
    String text;

    public McqVariable(String name, Group group, VariableType type) {
        super(name, group, type);
        if (type != VariableType.BOOLEAN) {
            log.warn(String.format("%s type given when creating MCQ \"%s\"", type, name));
            log.warn("Type of a MCQ variable should be BOOLEAN.");
        }
    }

    /** Builder. Variable type is BOOLEAN. */
    @Builder
    public McqVariable(String name, Group group, String mcqName, String text) {
        super(name, group, VariableType.BOOLEAN);
        this.mqcName = mcqName;
        this.text = text;
    }
}
