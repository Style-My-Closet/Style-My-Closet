package com.stylemycloset.weather.batch;

import com.stylemycloset.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherScheduler {

    private final WeatherService weatherService;
    private final JobLauncher jobLauncher;
    private final Job fetchWeatherJob;

    @Scheduled(cron = "0 0 * * * *") // 매 정각 실행
    public void runWeatherJob() {
        try {
            JobParameters parameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis()) // 중복 실행 방지
                .toJobParameters();

            jobLauncher.run(fetchWeatherJob, parameters);

        } catch (Exception e) {
            // 로깅 및 예외 처리
            e.printStackTrace();
        }
    }
}
