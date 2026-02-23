package fr.insee.kraftwerk.api.dto;

import fr.insee.kraftwerk.core.data.model.InterrogationId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DebugJsonExport {
    private List<InterrogationId> interrogationIds;
}
