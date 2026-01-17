package com.k2so.watcher.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Scheduler configuration is handled via application.yml and @EnableScheduling
}
