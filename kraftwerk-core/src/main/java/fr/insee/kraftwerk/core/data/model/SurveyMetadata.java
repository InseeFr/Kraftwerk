package fr.insee.kraftwerk.core.data.model;

import lombok.Builder;

import java.util.Map;

/**
 * This class represents a survey metadata obtained from Genesis database
 */
@Builder
public record SurveyMetadata(
        String campaignId,
        String questionnaireId,
        Mode mode,
        Map<String, SurveyMetadataVariable> variableDocumentMap
){}