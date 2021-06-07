package io.spring.flows.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Basic Batch Running Configuration
 */
@Configuration
public class FlowLastConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private Step myFirstStep;

//    @Bean
//    public TaskletStep myFirstStep() {
//        return stepBuilderFactory.get("myFirstStep")
//                .tasklet((stepContribution, chunkContext) -> {
//                    System.out.println("myFirstStep Batch Running...");
//                    return RepeatStatus.FINISHED;
//                }).build();
//    }

    @Bean
    public Job executeLastJob(Flow flow){
        return jobBuilderFactory.get("executeLastJob")
                .start(myFirstStep)
                .on("COMPLETED")
                .to(flow)
                .end()
                .build();
    }
}
