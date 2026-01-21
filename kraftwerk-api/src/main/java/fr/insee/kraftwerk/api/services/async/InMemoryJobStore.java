package fr.insee.kraftwerk.api.services.async;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryJobStore {

    private final ConcurrentMap<String, JobExecution> jobs = new ConcurrentHashMap<>();

    public void start(String jobId) {
        jobs.put(jobId, new JobExecution(
                jobId,
                JobStatus.RUNNING,
                null,
                Instant.now(),
                null
        ));
    }

    public void success(String jobId) {
        jobs.computeIfPresent(jobId, (id, job) ->
                new JobExecution(
                        id,
                        JobStatus.SUCCESS,
                        null,
                        job.startedAt(),
                        Instant.now()
                )
        );
    }

    public void fail(String jobId, Exception e) {
        jobs.computeIfPresent(jobId, (id, job) ->
                new JobExecution(
                        id,
                        JobStatus.FAILED,
                        e.getMessage(),
                        job.startedAt(),
                        Instant.now()
                )
        );
    }

    public Optional<JobExecution> get(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }
}
