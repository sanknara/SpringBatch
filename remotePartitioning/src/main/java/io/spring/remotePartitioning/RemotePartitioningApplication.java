package io.spring.remotePartitioning;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class RemotePartitioningApplication {

	public static void main(String[] args) {
		SpringApplication.run(RemotePartitioningApplication.class, args);
	}

}
