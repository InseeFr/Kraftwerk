package fr.insee.kraftwerk.core.metadata;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;

@Log4j2
@Getter
public enum ComponentLunatic {

    DATE_PICKER("Datepicker", VariableType.DATE),
    CHECKBOX_BOOLEAN("CheckboxBoolean", VariableType.BOOLEAN),
    INPUT_NUMBER("InputNumber", null),
    INPUT("Input", VariableType.STRING),
    TEXT_AREA("Textarea", VariableType.STRING),
    RADIO("Radio", VariableType.STRING),
    CHECKBOX_ONE("CheckboxOne", VariableType.STRING),
    DROPDOWN("Dropdown", VariableType.STRING),
    CHECKBOX_GROUP("CheckboxGroup", VariableType.BOOLEAN),
    SUGGESTER("Suggester", VariableType.STRING),
    PAIRWISE_LINKS("PairwiseLinks", null),
    TABLE("Table", null);

    private String jsonName;
    // Represents the type of the variable expected with this component type
    // If null, the type is not unique
    private VariableType type;

    ComponentLunatic(String jsonName, VariableType type) {
        this.jsonName=jsonName;
        this.type = type;
    }

    public static ComponentLunatic fromJsonName(String jsonName) {
        return Arrays.stream(ComponentLunatic.values())
                .filter(component -> component.getJsonName().equals(jsonName))
                .findFirst()
                .orElse(null);
    }
}
