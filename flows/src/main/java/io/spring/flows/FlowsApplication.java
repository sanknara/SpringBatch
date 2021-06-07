package io.spring.flows;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class FlowsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlowsApplication.class, args);
	}

}
