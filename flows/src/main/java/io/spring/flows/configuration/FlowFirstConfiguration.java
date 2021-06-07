package io.spring.flows.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Basic Batch Running Configuration
 */
@Configuration
public class FlowFirstConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
//    @Primary
    public TaskletStep myFirstStep() {
        return stepBuilderFactory.get("myFirstStep")
                .tasklet((stepContribution, chunkContext) -> {
                        System.out.println("myFirstStep Batch Running...");
                        return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Job executeFirstJob(Flow flow){
        return jobBuilderFactory.get("executeFirstJob")
                .start(flow)
                .next(myFirstStep())
                .end()
                .build();
    }
}
