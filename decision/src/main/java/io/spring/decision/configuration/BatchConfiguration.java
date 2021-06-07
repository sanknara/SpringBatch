package io.spring.decision.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step startStep() {
        return stepBuilderFactory.get("startStep")
                        .tasklet((stepContribution, chunkContext) -> {
                            System.out.println("Start Tasklet");
                            return RepeatStatus.FINISHED;
                        }).build();
    }

    @Bean
    public Step evenStep() {
        return stepBuilderFactory.get("evenStep")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("Even Tasklet");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step oddStep() {
        return stepBuilderFactory.get("oddStep")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("Odd Tasklet");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public JobExecutionDecider decider(){
        return new OddDecidier();
    }

    @Bean
    public Job job1(){
        return jobBuilderFactory.get("job1")
                .start(startStep())
                .next(decider())
                .from(decider()).on("ODD").to(oddStep())
                .from(decider()).on("EVEN").to(evenStep())
                .from(oddStep()).on("*").to(decider())
                .from(decider()).on("ODD").to(oddStep())
                .from(decider()).on("EVEN").to(evenStep())
                .end().build();
    }


    public static class OddDecidier implements JobExecutionDecider{
        private int count = 0;
        @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution){
            count++;

            return count % 2 == 0 ? new FlowExecutionStatus("EVEN"):
                    new FlowExecutionStatus("ODD");
        }
    }

}
