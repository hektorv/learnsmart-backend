package com.learnsmart.profile.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Enable async processing for audit logging
    // Uses default SimpleAsyncTaskExecutor
    // For production, consider configuring a ThreadPoolTaskExecutor
}
