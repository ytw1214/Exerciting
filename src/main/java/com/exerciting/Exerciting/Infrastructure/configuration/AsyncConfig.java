package com.exerciting.Exerciting.Infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name="kboExecutor")
    public ThreadPoolTaskExecutor setKboExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(5);
        executor.setThreadNamePrefix("kbo-date-crawling");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //shutdown시 task가 처리될때까지 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //종료될때까지 대기가 아닌 해당 시간만큼 대기 후 shutdown
        executor.setAwaitTerminationSeconds(30);

        return executor;
    }
}
