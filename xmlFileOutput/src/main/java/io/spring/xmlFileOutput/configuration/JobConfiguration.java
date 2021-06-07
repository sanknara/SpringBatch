package io.spring.xmlFileOutput.configuration;

import io.spring.xmlFileOutput.domain.Customer;
import io.spring.xmlFileOutput.domain.CustomerRowMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

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
    public ItemWriter<Object> customerWriter() throws Exception {
        StaxEventItemWriter<Customer> itemWriter = new StaxEventItemWriter<Customer>();
        XStreamMarshaller marshaller = new XStreamMarshaller();

        Map<String, Object>  aliases=new HashMap<>();
        aliases.put("customer", Customer.class);

        marshaller.setAliases(aliases);

        itemWriter.setRootTagName("customers");
        itemWriter.setMarshaller(marshaller);
        String customerOutputPath = File.createTempFile("customerXMLOutput",
                "xml").getAbsolutePath();
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
                .writer(customerWriter())
                .build();
    }

    @Bean
    public Job outputXmlFileJob() throws Exception {
        return jobBuilderFactory.get("outputXmlFileJob")
                .start(step222()).build();
    }

}
