package com.stylemycloset.common.config;

import com.stylemycloset.weather.batch.WeatherFetchTasklet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final WeatherFetchTasklet weatherFetchTasklet;

    @Bean
    public Job weatherJob(JobRepository jobRepository, Step weatherFetchStep) {
        return new JobBuilder("weatherJob", jobRepository)
            .start(weatherFetchStep)
            .build();
    }

    @Bean
    public Step weatherFetchStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("weatherFetchStep", jobRepository)
            .tasklet(weatherFetchTasklet, transactionManager)
            .build();
    }


    @Bean
    public ItemReader<String> simpleReader() {
        return new ListItemReader<>(List.of("a", "b", "c"));
    }

    @Bean
    public ItemProcessor<String, String> simpleProcessor() {
        return item -> item.toUpperCase();  // 간단한 처리 예
    }

    @Bean
    public ItemWriter<String> simpleWriter() {
        return items -> items.forEach(System.out::println);
    }
}
