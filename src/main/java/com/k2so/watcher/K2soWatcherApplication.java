package com.k2so.watcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class K2soWatcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(K2soWatcherApplication.class, args);
    }
}
