package fr.insee.kraftwerk.api.dto;

import fr.insee.kraftwerk.core.data.model.InterrogationId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DebugJsonExportResultDto {
    private String collectionInstrumentId;
    private List<InterrogationId> successIds;
    private List<DebugErrorDto> errors;
}
