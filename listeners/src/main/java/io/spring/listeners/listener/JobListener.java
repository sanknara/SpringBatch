package io.spring.listeners.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class JobListener implements JobExecutionListener {

//    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    public JobListener(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();

        SimpleMailMessage mail =
                getSimpleMessage(String.format("%s is starting", jobName),
                        String.format("As per your request," +
                                "we are informing you that " +
                                "%s is starting", jobName));
        mailSender.send(mail);
    }

    private SimpleMailMessage getSimpleMessage(String subject, String text) {
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo("sankarkarthik007@gmail.com");
        mail.setSubject(subject);
        mail.setText(text);
        return mail;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();

        SimpleMailMessage mail =
                getSimpleMessage(String.format("%s is completed", jobName),
                        String.format("As per your request," +
                                "we are informing you that " +
                                "%s is completed", jobName));
        mailSender.send(mail);
    }
}
