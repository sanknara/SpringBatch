package io.spring.remoteChunking.configuration;

import io.spring.remoteChunking.domain.ColumnRangePartitioner;
import io.spring.remoteChunking.domain.Customer;
import io.spring.remoteChunking.domain.CustomerRowMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.partition.BeanFactoryStepLocator;
import org.springframework.batch.integration.partition.MessageChannelPartitionHandler;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.scheduling.support.PeriodicTrigger;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class JobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Autowired
    public JobExplorer jobExplorer;

    @Autowired
    private JobRepository jobRepository;

    private ApplicationContext applicationContext;

    @Bean
    public PartitionHandler partitionHandler(MessagingTemplate mt) throws Exception{

        MessageChannelPartitionHandler partitionHandler = new MessageChannelPartitionHandler();

        partitionHandler.setGridSize(2);
        partitionHandler.setStepName("slaveStep");
        partitionHandler.setMessagingOperations(mt);
        partitionHandler.setPollInterval(5000l);
        partitionHandler.setJobExplorer(jobExplorer);

        partitionHandler.afterPropertiesSet();

        return partitionHandler;
    }

    @Bean
    @Profile("slave")
    @ServiceActivator(inputChannel = "inboundRequests", outputChannel = "outboundStaging")
    public StepExecutionRequestHandler stepExecutionRequestHandler(){
        StepExecutionRequestHandler stepExecutionRequestHandler = new StepExecutionRequestHandler();

        BeanFactoryStepLocator stepLocator = new BeanFactoryStepLocator();
        stepLocator.setBeanFactory(applicationContext);
        stepExecutionRequestHandler.setStepLocator(stepLocator);
        stepExecutionRequestHandler.setJobExplorer(jobExplorer);

        return stepExecutionRequestHandler;
    }

    @Bean(name= PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata defaultPoller(){
        PollerMetadata pollerMetadata = new PollerMetadata();
        pollerMetadata.setTrigger(new PeriodicTrigger(10));
        return pollerMetadata;
    }

    @Bean
    public ColumnRangePartitioner partitioner(){
    ColumnRangePartitioner partitioner = new ColumnRangePartitioner();

    partitioner.setColumn("id");
    partitioner.setDataSource(dataSource);
    partitioner.setTable("customer");

    return partitioner;
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<Customer> pagingItemReader(){
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(dataSource);
        reader.setFetchSize(5);
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
    public ItemProcessor<Customer, Customer> upperCaseItemProcessor() throws Exception {
        return (ItemProcessor<Customer, Customer>)customer -> new Customer(customer.getId(),
                customer.getFirstName().toUpperCase(),
                customer.getLastName().toUpperCase(),
                customer.getBirthDate());
    }

    @Bean
    @StepScope
    public ItemWriter customerItemWriter(){
        JdbcBatchItemWriter<Customer> writer = new JdbcBatchItemWriter<>();

        writer.setDataSource(dataSource);
        writer.setSql("INSERT INTO NEW_CUSTOMER VALUES(:id,:firstName,:lastName,:birthDate)");
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.afterPropertiesSet();
        return (ItemWriter)writer;
    }

    @Bean
    public Step slaveStep() throws Exception {
        return stepBuilderFactory.get("slaveStep")
                .chunk(5)
                .reader(pagingItemReader())
                .processor((Function<? super Object, ?>) upperCaseItemProcessor())
                .writer(customerItemWriter())
                .build();
    }

    @Bean
    @Profile("master")
    public Job partitionJob2() throws Exception {
        return jobBuilderFactory.get("partitionJob2")
                .start(slaveStep()).build();
    }

}
