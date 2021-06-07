package io.spring.startingAJob.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class JobLaunchingController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private Job job;


    @RequestMapping(value = "/", method= RequestMethod.POST)
//    @ResponseStatus(HttpStatus.ACCEPTED)
    public void launch(@RequestParam("name") String name) throws Exception{
        JobParameters jp = new JobParametersBuilder().addString("name",name).toJobParameters();

//        this.jobLauncher.run(job, jp);
    this.jobOperator.start("job", String.format("name=%s", name));

    }

    @RequestMapping(value = "/{id}", method= RequestMethod.DELETE)
//    @ResponseStatus(HttpStatus.OK)
    public void stop(@PathVariable("id") Long id) throws Exception{

//        this.jobLauncher.run(job, jp);
        this.jobOperator.stop(id);

    }

}
