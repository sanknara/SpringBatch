package io.spring.transitions.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Basic Batch Running Configuration
 */
@Configuration
@EnableBatchProcessing
public class StepTransitionConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public TaskletStep batchStep1() {
        return stepBuilderFactory.get("batchStep1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("First Batch Running...");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }

    @Bean
    public TaskletStep batchStep2() {
        return stepBuilderFactory.get("batchStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("Second Batch Running...");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }

    @Bean
    public TaskletStep batchStep3() {
        return stepBuilderFactory.get("batchStep3")
                .tasklet((stepContribution, chunkContext) -> {
                        System.out.println("Third Batch Running...");
                        return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Job batchTransitions4(){
        return jobBuilderFactory.get("batchTransitions4")
                .start(batchStep1())
                .on("COMPLETED")
                .to(batchStep2())
                .from(batchStep2())
                .on("COMPLETED")
                .stopAndRestart(batchStep3())
                .from(batchStep3())
                .end()
                .build();
    }
}
