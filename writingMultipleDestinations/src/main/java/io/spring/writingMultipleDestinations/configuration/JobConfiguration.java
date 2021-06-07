package io.spring.writingMultipleDestinations.configuration;

import io.spring.writingMultipleDestinations.domain.Customer;
import io.spring.writingMultipleDestinations.domain.CustomerClassifier;
import io.spring.writingMultipleDestinations.domain.CustomerRowMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.sql.DataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public ItemWriter<Object> customerXMLWriter() throws Exception {
        StaxEventItemWriter<Customer> itemWriter = new StaxEventItemWriter<Customer>();
        XStreamMarshaller marshaller = new XStreamMarshaller();

        Map<String, Object>  aliases=new HashMap<>();
        aliases.put("customer", Customer.class);

        marshaller.setAliases(aliases);

        itemWriter.setRootTagName("customers");
        itemWriter.setMarshaller(marshaller);
        String customerOutputPath = File.createTempFile("customer1XMLOutput",
                ".xml").getAbsolutePath();
        System.out.println(">> Output Path : "+customerOutputPath);
        itemWriter.setResource(new FileSystemResource(customerOutputPath));
        itemWriter.afterPropertiesSet();

        return (ItemWriter)itemWriter;
    }

    @Bean
    public ItemWriter<? super Object> itemWriter() throws Exception{
        List<ItemWriter<? super Customer>> writerList = new ArrayList<>(2);
        writerList.add(customerWriter());
        writerList.add(customerXMLWriter());

        CompositeItemWriter<Customer> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(writerList);
        compositeItemWriter.afterPropertiesSet();

        return (ItemWriter)compositeItemWriter;
    }

    @Bean
    public ItemWriter<? super Object> ownItemWriter() throws Exception{

        ClassifierCompositeItemWriter<Customer> compositeItemWriter = new ClassifierCompositeItemWriter<>();
        compositeItemWriter.setClassifier(new CustomerClassifier(customerXMLWriter(), customerWriter()));

        return (ItemWriter)compositeItemWriter;
    }

    @Bean
    public Step step222() throws Exception {
        return stepBuilderFactory.get("step222")
                .chunk(10)
                .reader(pagingItemReader())
                .writer(itemWriter())
                .stream((ItemStream)customerXMLWriter())
                .stream((ItemStream)customerWriter())
                .build();
    }

    @Bean
    public Job multipleOutputJob1() throws Exception {
        return jobBuilderFactory.get("multipleOutputJob1")
                .start(step222()).build();
    }

}
