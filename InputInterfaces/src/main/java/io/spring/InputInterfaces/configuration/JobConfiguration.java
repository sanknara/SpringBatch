package io.spring.InputInterfaces.configuration;

import io.spring.InputInterfaces.reader.StatelessItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class JobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public StatelessItemReader statelessItemReader(){
        List<String> data = new ArrayList<>();

        data.add("Foo");
        data.add("Bar");
        data.add("Baz");

        return new StatelessItemReader(data);
    }

    @Bean
    public TaskletStep step112(){
        return stepBuilderFactory.get("step111")
                .<String,String>chunk(3)
                .reader(statelessItemReader())
                .writer(list -> {
                    for(Object item : list){
                        System.out.println("item = "+item.toString());
                    }
                })
                .build();
    }

    @Bean
    public Job interfacesJob(){
        return jobBuilderFactory.get("interfacesJob")
                .start(step112()).build();
    }

}
