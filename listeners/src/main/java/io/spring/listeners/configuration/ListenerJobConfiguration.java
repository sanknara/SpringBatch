package io.spring.listeners.configuration;

import io.spring.listeners.listener.JobListener;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Arrays;
import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class ListenerJobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

//    @Autowired
//    private JavaMailSender mailSender;

//    @Bean
//    public JavaMailSenderImpl mailSender() {
//        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
//
//        javaMailSender.setProtocol("SMTP");
//        javaMailSender.setHost("127.0.0.1");
//        javaMailSender.setPort(25);
//
//        return javaMailSender;
//    }

    @Bean
    public ItemReader<? super Object> reader(){
        return new ListItemReader<>(Arrays.asList("one","two","three"));
    }

    @Bean
    public ItemWriter<Object> writer(){
        return (List <?> items) -> {
            for(Object item:items){
                System.out.println("Writing Item "+ item);
            }
        };
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .chunk(2)
                .faultTolerant()
                .listener(ChunkListener.class)
                .reader(reader())
                .writer(writer())
                .build();
    }

    @Bean
    public Job listenerJob(JavaMailSender javaMailSender) {
        return jobBuilderFactory.get("listenerJob")
                .start(step1())
                .listener(new JobListener(javaMailSender)).build();
    }

}
