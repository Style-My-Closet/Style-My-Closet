package com.stylemycloset.common.config;

import com.stylemycloset.common.config.taskdecorator.MdcTaskDecorator;
import com.stylemycloset.common.config.taskdecorator.SecurityTaskDecorator;
import java.util.List;
import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.CompositeTaskDecorator;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableRetry
public class AsyncConfig {

  @Bean(name = "eventTaskExecutor")
  public TaskExecutor eventExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("event-task-");
    executor.setTaskDecorator(taskDecorator());
    executor.initialize();
    return executor;
  }

  @Bean(name = "sseTaskExecutor")
  public TaskExecutor sseExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("sse-task-");
    executor.setTaskDecorator(taskDecorator());
    executor.initialize();
    return executor;
  }

  @Bean("uploadExecutor")
  public Executor s3Executor(TaskDecorator taskDecorator) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(3);
    executor.setThreadNamePrefix("S3-Thread-");
    executor.setTaskDecorator(taskDecorator);
    executor.initialize();

    return executor;
  }

  @Bean
  public TaskDecorator taskDecorator() {
    return new CompositeTaskDecorator(
        List.of(
            new MdcTaskDecorator(),
            new SecurityTaskDecorator()
        )
    );
  }

}
