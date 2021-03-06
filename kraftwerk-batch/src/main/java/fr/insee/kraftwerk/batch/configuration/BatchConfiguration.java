package fr.insee.kraftwerk.batch.configuration;

import fr.insee.kraftwerk.batch.Launcher;
import fr.insee.kraftwerk.batch.UserVtlBatch;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Value("${fr.insee.kraftwerk.inDirectory}")
	private String inDirectory;

	@Value("${spring.batch.job.names}")
	private String[] jobEnabled;

	@Autowired
	Environment environment;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	private final Launcher launcher=new Launcher();

	@Bean
	public Job batchJob(Step stepEtl) {
		return this.jobBuilderFactory.get("batchJob")
				.start(stepEtl)
				.build();
	}

	@Bean
	public Step stepEtl() throws IOException {
		Function<Path, Boolean> processor=launcher::main;
		SimpleStepBuilder<Path, Boolean> simpleStepBuilder=this.stepBuilderFactory.get("stepEtl")
				.chunk(1);
		return simpleStepBuilder.reader(new DirectoryItemReader(inDirectory))
				.processor(processor)
				.writer(b->{})
				.build();
	}


	@Bean
	protected Step userVtlStep() {
		UserVtlBatch userVtlBatch = new UserVtlBatch();
		if (Arrays.asList(jobEnabled).contains("userVtlJob")) {
			String campaignName = environment.getProperty("campaignName");
			if (campaignName != null) {
				userVtlBatch.setCampaignDirectory(Path.of(inDirectory).resolve(campaignName));
			} else {
				throw new RuntimeException("Missing value for parameter 'campaignName'.");
			}
		}
		return stepBuilderFactory.get("userVtlStep").tasklet(userVtlBatch).build();
	}

	@Bean
	protected Job userVtlJob() {
		return jobBuilderFactory.get("userVtlJob").start(userVtlStep()).build();
	}

}
