package io.spring.schedulingAJob.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;
import java.util.Date;

@Configuration
public class JobConfiguration extends DefaultBatchConfigurer implements ApplicationContextAware {

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

    @Autowired
    private JobRegistry jobRegistry;

    @Autowired
    private JobLauncher jobLauncher;

    private ApplicationContext applicationContext;


    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() throws Exception{
        JobRegistryBeanPostProcessor register = new JobRegistryBeanPostProcessor();

        register.setJobRegistry(jobRegistry);
        register.setBeanFactory(applicationContext.getAutowireCapableBeanFactory());
        register.afterPropertiesSet();

        return register;
    }


    @Bean
    public JobOperator jobOperator() throws Exception{
        SimpleJobOperator simpleJobOperator = new SimpleJobOperator();

        simpleJobOperator.setJobLauncher(jobLauncher);
        simpleJobOperator.setJobParametersConverter(new DefaultJobParametersConverter());
        simpleJobOperator.setJobRepository(jobRepository);
        simpleJobOperator.setJobExplorer(jobExplorer);
        simpleJobOperator.setJobRegistry(jobRegistry);

        simpleJobOperator.afterPropertiesSet();

        return simpleJobOperator;
    }

    @Bean
    @StepScope
    public Tasklet tasklet(){
        return ((stepContribution, chunkContext) -> {
            System.out.println(String.format("I was run at %s", new Date()));
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Job scheduleJob(){
        return jobBuilderFactory.get("scheduleJob")
                .start(stepBuilderFactory.get("step1").tasklet(tasklet()).build()).build();
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

    }

    @Override
    public JobLauncher getJobLauncher(){
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        try{
//            jobLauncher = new SimpleJobLauncher();
            launcher.setJobRepository(jobRepository);
            launcher.setTaskExecutor(new SimpleAsyncTaskExecutor());
            launcher.afterPropertiesSet();
        }catch(Exception e){
            e.printStackTrace();
        }
        return jobLauncher;
    }
}
