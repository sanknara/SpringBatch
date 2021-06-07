package io.spring.itemProcessorInterface.configuration;

import io.spring.itemProcessorInterface.domain.Customer;
import io.spring.itemProcessorInterface.domain.CustomerRowMapper;
import io.spring.itemProcessorInterface.processor.UpperCaseItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    @Bean
    public JdbcPagingItemReader<Customer> pagingItemReader(){
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(dataSource);
        reader.setFetchSize(10);
        reader.setRowMapper(new CustomerRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id,firstName,lastName,birthDate");
        queryProvider.setFromClause("from customer");

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);

        reader.setQueryProvider(queryProvider);

        return  reader;
    }

    @Bean
    public ItemProcessor<? super Object, ?> itemProcessor() {
        return (ItemProcessor)new UpperCaseItemProcessor();
    }

    @Bean
    public ItemWriter<Object> customerWriter() throws Exception {
        FlatFileItemWriter<Customer> itemWriter = new FlatFileItemWriter<Customer>();

        itemWriter.setLineAggregator(new PassThroughLineAggregator<>());
//        itemWriter.setLineAggregator(new CustomerLineAggregator());
        String customerOutputPath = File.createTempFile("customer1Output",
                ".out").getAbsolutePath();
        System.out.println(">> Output Path : "+customerOutputPath);
        itemWriter.setResource(new FileSystemResource(customerOutputPath));
        itemWriter.afterPropertiesSet();

        return (ItemWriter)itemWriter;
    }

    @Bean
    public Step step222() throws Exception {
        return stepBuilderFactory.get("step222")
                .chunk(10)
                .reader(pagingItemReader())
                .processor(itemProcessor())
                .writer(customerWriter())
                .build();
    }

    @Bean
    public Job processorJob() throws Exception {
        return jobBuilderFactory.get("processorJob")
                .start(step222()).build();
    }

}
