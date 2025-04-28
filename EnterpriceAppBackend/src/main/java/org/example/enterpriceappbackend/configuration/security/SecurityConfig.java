package org.example.enterpriceappbackend.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disabilita la protezione CSRF
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/**").permitAll()  // Consente l'accesso a tutte le pagine senza login
                        .anyRequest().authenticated()  // Richiede l'autenticazione per altre richieste
                );

        return http.build();
    }
}
