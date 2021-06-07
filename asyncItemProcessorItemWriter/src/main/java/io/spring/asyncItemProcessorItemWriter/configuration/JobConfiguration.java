package io.spring.asyncItemProcessorItemWriter.configuration;

import io.spring.asyncItemProcessorItemWriter.domain.Customer;
import io.spring.asyncItemProcessorItemWriter.domain.CustomerRowMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Configuration
public class JobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

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
        reader.setSaveState(false);

        return  reader;
    }

    @Bean
    public ItemProcessor itemProcessor() {
        return (ItemProcessor)new ItemProcessor<Customer, Customer>() {
            @Override
            public Customer process(Customer customer) throws Exception {
                Thread.sleep(new Random().nextInt(10));
                return new Customer(customer.getId(),
                        customer.getFirstName().toUpperCase(),
                        customer.getLastName().toUpperCase(),
                        customer.getBirthDate());
            }
        };
    }

    @Bean
    public AsyncItemProcessor asyncItemProcessor() throws Exception{
        AsyncItemProcessor<Customer, Customer> asyncItemProcessor = new AsyncItemProcessor<>();

        asyncItemProcessor.setDelegate(itemProcessor());
        asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor());
        asyncItemProcessor.afterPropertiesSet();

        return asyncItemProcessor;
    }

    @Bean
    public ItemWriter customerItemWriter(){
        JdbcBatchItemWriter<Customer> writer = new JdbcBatchItemWriter<>();

        writer.setDataSource(dataSource);
        writer.setSql("INSERT INTO NEW_CUSTOMER VALUES(:id,:firstName,:lastName,:birthDate)");
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.afterPropertiesSet();
        return (ItemWriter)writer;
    }

    @Bean
    public AsyncItemWriter asyncItemWriter() throws Exception{
        AsyncItemWriter<Customer> asyncItemProcessor = new AsyncItemWriter<>();

        asyncItemProcessor.setDelegate(customerItemWriter());
        asyncItemProcessor.afterPropertiesSet();

        return asyncItemProcessor;
    }

    @Bean
    public Step step222() throws Exception {
        return stepBuilderFactory.get("step222")
                .chunk(10)
                .reader(pagingItemReader())
                .processor(itemProcessor())
//                .processor(asyncItemProcessor())
                .writer(customerItemWriter())
//                .writer(asyncItemWriter())
                .build();
    }

    @Bean
    public Job asyncProccessingJob() throws Exception {
        return jobBuilderFactory.get("asyncProccessingJob")
                .start(step222()).build();
    }

}
