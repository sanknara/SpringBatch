package io.spring.readingXML.configuration;

import io.spring.readingXML.domain.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public StaxEventItemReader<Customer> customerItemReader() {

        XStreamMarshaller unMarshaller = new XStreamMarshaller();
        Map<String, Object>  aliases=new HashMap<>();
        aliases.put("customer", Customer.class);

        unMarshaller.setAliases(aliases);

        StaxEventItemReader<Customer> reader = new StaxEventItemReader<>();

        reader.setResource(new ClassPathResource("data/customers.xml"));
        reader.setUnmarshaller(unMarshaller);
        reader.setFragmentRootElementName("customer");
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
                .reader(customerItemReader())
                .writer(customerItemWriter())
                .build();
    }

    @Bean
    public Job job444(){
        return jobBuilderFactory.get("job444")
                .start(step222()).build();
    }

}
