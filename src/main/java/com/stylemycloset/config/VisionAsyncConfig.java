package com.stylemycloset.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class VisionAsyncConfig {
    @Bean(name = "visionExecutor")
    public Executor visionExecutor(TaskDecorator taskDecorator) {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(2);
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(50);
        ex.setThreadNamePrefix("vision-");
        ex.setTaskDecorator(taskDecorator);
        ex.initialize();
        return ex;
    }
}


