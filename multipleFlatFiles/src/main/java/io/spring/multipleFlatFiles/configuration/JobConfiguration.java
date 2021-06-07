package io.spring.multipleFlatFiles.configuration;

import io.spring.multipleFlatFiles.domain.Customer;
import io.spring.multipleFlatFiles.domain.CustomerFieldSetMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class JobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Value("classpath*:/data/customer*.csv")
    private Resource[] inputFiles;


    @Bean
    public MultiResourceItemReader<Customer> multiResourceItemReader() {
        MultiResourceItemReader<Customer> reader = new MultiResourceItemReader<>();

        reader.setDelegate(customerItemReader());
        reader.setResources(inputFiles);

        return reader;
    }


    @Bean
    public FlatFileItemReader<Customer> customerItemReader() {
        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();

        reader.setLinesToSkip(1);
        reader.setResource(new ClassPathResource("/data/customer1.csv"));

        DefaultLineMapper<Customer> customerLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(new String[]{"id", "firstName","lastName","birthDate"});

        customerLineMapper.setLineTokenizer(tokenizer);
        customerLineMapper.setFieldSetMapper(new CustomerFieldSetMapper());
        customerLineMapper.afterPropertiesSet();

        reader.setLineMapper(customerLineMapper);
                return  reader;
    }

    @Bean
    public ItemWriter<Object> customerItemWriter(){
        return items -> {
            for(Object item : items){
                System.out.println(item);
            }
        };
    }

    @Bean
    public Step step222(){
        return stepBuilderFactory.get("step222")
                .chunk(10)
                .reader(multiResourceItemReader())
                .writer(customerItemWriter())
                .build();
    }

    @Bean
    public Job job555(){
        return jobBuilderFactory.get("job555")
                .start(step222()).build();
    }

}
