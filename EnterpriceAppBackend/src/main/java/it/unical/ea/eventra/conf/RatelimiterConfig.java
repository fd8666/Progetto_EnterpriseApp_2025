package it.unical.ea.eventra.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.util.concurrent.RateLimiter;


//Gestisce il numero di richieste alle api globale in un determinato lasso di tempo
@Configuration
public class RatelimiterConfig {
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(15);
    }
}