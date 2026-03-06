package fr.insee.kraftwerk.api.services.async;

import fr.insee.kraftwerk.api.dto.ExportCheckResultDto;
import fr.insee.kraftwerk.api.dto.ExportJobResultDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryExportJobStore {

    private final Map<String, ExportJobResultDto> jobsMap = new ConcurrentHashMap<>();

    public void start(String jobId) {
        jobsMap.put(
                jobId,
                ExportJobResultDto.builder()
                        .status(ExportJobStatus.RUNNING)
                        .errors(new ArrayList<>())
                        .startTime(LocalDateTime.now())
                        .build()
        );
    }

    public void complete(String jobId, ExportCheckResultDto result, List<String> errors) {

        jobsMap.computeIfPresent(jobId, (id, job) -> {
            job.setCheckResult(result);
            job.setErrors(errors);
            job.setEndTime(LocalDateTime.now());

            job.setStatus(errors == null || errors.isEmpty()
                    ? ExportJobStatus.DONE
                    : ExportJobStatus.PARTIAL);

            return job;
        });
    }

    public void fail(String jobId, Exception e) {
        jobsMap.computeIfPresent(jobId, (id, job) -> {

            job.getErrors().add(e.getMessage());
            job.setStatus(ExportJobStatus.ERROR);
            job.setEndTime(LocalDateTime.now());

            return job;
        });
    }

    public Optional<ExportJobResultDto> get(String jobId) {
        return Optional.ofNullable(jobsMap.get(jobId));
    }
}