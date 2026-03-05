package fr.insee.kraftwerk.api.dto;

import fr.insee.kraftwerk.api.services.async.ExportJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Builder
@Data
public class ExportJobResultDto {

    private ExportJobStatus status;
    private ExportCheckResultDto checkResult;
    private List<String> errors;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
