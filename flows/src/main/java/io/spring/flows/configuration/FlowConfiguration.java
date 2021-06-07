package io.spring.flows.configuration;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
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
public class FlowConfiguration {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public TaskletStep batchStep1() {
        return stepBuilderFactory.get("batchStep1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("First Batch Running from flowSankar...");
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
                        System.out.println("Second Batch Running from flowSankar...");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }

    /**
     * Flow Sankar used to execute batchStep1 and batchStep2
     * @return Flow
     */
    @Bean
    public Flow flowSankar() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flowSankar");
        flowBuilder.start(batchStep1())
                .next(batchStep2())
                .end();
        return flowBuilder.build();
    }
}
