package io.spring.skipRetryListeners.configuration;
import io.spring.skipRetryListeners.components.CustomRetryableException;
import io.spring.skipRetryListeners.components.CustomSkipListener;
import io.spring.skipRetryListeners.components.SkipItemProcessor;
import io.spring.skipRetryListeners.components.SkipItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
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
    public ListItemReader reader(){
        List<String> items = new ArrayList<>();
        for(int i=0; i < 100; i++){
            items.add(String.valueOf(i));
        }

        ListItemReader<String> reader = new ListItemReader<>(items);

        return reader;
    }


    @Bean
    @StepScope
    public ItemProcessor processor(){
       return new SkipItemProcessor();
    }

    @Bean
    @StepScope
    public ItemWriter writer(){
        return new SkipItemWriter();
    }

    @Bean
    public Step step1005() throws Exception {
        return stepBuilderFactory.get("step1005")
                .chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .faultTolerant()
                .skip(CustomRetryableException.class)
                .skipLimit(15)
                .listener(new CustomSkipListener())
                .build();
    }

    @Bean
    public Job skipListenerCheckJob5() throws Exception {
        return jobBuilderFactory.get("skipListenerCheckJob5")
                .start(step1005()).build();
    }

}
