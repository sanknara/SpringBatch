package io.spring.itemStream.configuration;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
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
   public StatefulItemReader itemReader(){
       List<String> items = new ArrayList<>();
       for(int i=1; i <= 100; i++){
           items.add(String.valueOf(i));
       }

       return new StatefulItemReader(items);
   }

    @Bean
    public ItemWriter<Object> itemWriter(){
        return items -> {
            for(Object item : items){
                System.out.println(">>"+item);
            }
        };
    }

    @Bean
    public Step step222(){
        return stepBuilderFactory.get("step222")
                .chunk(10)
                .reader(itemReader())
                .writer(itemWriter())
                .stream(itemReader())
                .build();
    }

    @Bean
    public Job statefulJob(){
        return jobBuilderFactory.get("statefulJob")
                .start(step222()).build();
    }

}
