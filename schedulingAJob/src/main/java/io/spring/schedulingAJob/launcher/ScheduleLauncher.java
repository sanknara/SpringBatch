package io.spring.schedulingAJob.launcher;

import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleLauncher {

    @Autowired
    private JobOperator jobOperator;

    @Scheduled(fixedDelay = 5000l)
    public void runJob() throws Exception{
        jobOperator.startNextInstance("scheduleJob");
    }
}
