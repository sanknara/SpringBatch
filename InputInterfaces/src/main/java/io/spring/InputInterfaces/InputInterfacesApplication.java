package io.spring.InputInterfaces;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class InputInterfacesApplication {

	public static void main(String[] args) {
		SpringApplication.run(InputInterfacesApplication.class, args);
	}

}
