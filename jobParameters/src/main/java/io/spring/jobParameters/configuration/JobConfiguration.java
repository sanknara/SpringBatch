package io.spring.jobParameters.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public Tasklet helloWorldTasklet(@Value("#{jobParameters['message']}")String message){
        return ((stepContribution, chunkContext) -> {
            System.out.println(message);
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public TaskletStep step111(){
        return stepBuilderFactory.get("step111")
                .tasklet(helloWorldTasklet(null))
                .build();
    }

    @Bean
    public Job jobParametersJob(){
        return jobBuilderFactory.get("jobParametersJob")
                .start(step111()).build();
    }

}
