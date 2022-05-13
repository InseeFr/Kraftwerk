package fr.insee.kraftwerk.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class Kraftwerk {

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(Kraftwerk.class, args)));
	}

}
