package fr.insee.kraftwerk.api.dto;

import fr.insee.kraftwerk.core.data.model.InterrogationId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DebugErrorDto {
    private InterrogationId interrogationId;
    private String usualSurveyUnitId;
    private String errorMessage;
}
