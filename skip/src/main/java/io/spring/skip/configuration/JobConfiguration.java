package io.spring.skip.configuration;
import io.spring.skip.components.CustomRetryableException;
import io.spring.skip.components.SkipItemProcessor;
import io.spring.skip.components.SkipItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

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
    public ItemProcessor processor(
            @Value("#{jobParameters['retry']}")String retry
    ){
       SkipItemProcessor processor = new SkipItemProcessor();
       processor.setSkip(StringUtils.hasText(retry) &&
               retry.equalsIgnoreCase("processor"));

       return (ItemProcessor) processor;
    }

    @Bean
    @StepScope
    public ItemWriter writer(
            @Value("#{jobParameters['retry']}")String retry
    ){
        SkipItemWriter writer = new SkipItemWriter();
        writer.setSkip(StringUtils.hasText(retry) &&
                retry.equalsIgnoreCase("writer"));

        return (ItemWriter)writer;
    }

    @Bean
    public Step step1005() throws Exception {
        return stepBuilderFactory.get("step1005")
                .chunk(10)
                .reader(reader())
                .processor(processor(null))
                .writer(writer(null))
                .faultTolerant()
                .skip(CustomRetryableException.class)
                .skipLimit(15)
                .build();
    }

    @Bean
    public Job skipCheckJob5() throws Exception {
        return jobBuilderFactory.get("skipCheckJob5")
                .start(step1005()).build();
    }

}
