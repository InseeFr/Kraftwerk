package fr.insee.kraftwerk.core.data.model;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.VariableType;
import lombok.Builder;

@Builder
public record SurveyMetadataVariable (
        String name,
        Group group,
        VariableType type,
        String sasFormat,
        int maxLengthData,
        String questionName,
        boolean isInQuestionGrid
){}
