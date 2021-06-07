package io.spring.databaseInput.configuration;

import io.spring.databaseInput.domain.Customer;
import io.spring.databaseInput.domain.CustomerRowMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Bean
    public JdbcCursorItemReader<Customer> cursorItemReader() {
        JdbcCursorItemReader<Customer> reader = new JdbcCursorItemReader<>();

        reader.setSql("select id,firstName,lastName,birthDate from customer order by lastName");
        reader.setDataSource(dataSource);
        reader.setRowMapper(new CustomerRowMapper());
                return  reader;
    }

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
                .reader(pagingItemReader())
                .writer(customerItemWriter())
                .build();
    }

    @Bean
    public Job job222(){
        return jobBuilderFactory.get("job222")
                .start(step222()).build();
    }

}
