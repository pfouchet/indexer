package com.groupeseb.reindexer;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobConfiguration {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Bean
    public Job reindexationJob(Step step) {
        return jobBuilderFactory.get("reindexationJob")
                .incrementer(new RunIdIncrementer())
                .flow(step)
                .end()
                .build();
    }
}
