package io.spring.startingAJob.configuration;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;

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
    public Tasklet tasklet(@Value("{jobParameters['name']")String name){
        return ((stepContribution, chunkContext) -> {
            System.out.println(String.format("%s is sleeping again", name));
            Thread.sleep(1000);
            return RepeatStatus.CONTINUABLE;
        });
    }

    @Bean
    public Job webAppJob(){
        return jobBuilderFactory.get("webAppJob")
                .start(stepBuilderFactory.get("step1").tasklet(tasklet(null)).build()).build();
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
