package org.example.enterpriceappbackend.configuration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.util.concurrent.RateLimiter;

@Configuration
public class RatelimiterConfig {
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(15);
    }
}