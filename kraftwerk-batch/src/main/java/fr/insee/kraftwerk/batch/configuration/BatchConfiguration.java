package fr.insee.kraftwerk.batch.configuration;

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

import fr.insee.kraftwerk.batch.Launcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Value("${fr.insee.kraftwerk.inDirectory}")
	private String inDirectory;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	private final Launcher launcher=new Launcher();

	@Bean
	public Job uniqueJob(Step stepEtl) {
		return this.jobBuilderFactory.get("uniqueJob")
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

}
