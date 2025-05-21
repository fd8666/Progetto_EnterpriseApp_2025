//package org.example.enterpriceappbackend.configuration.security;
//
//import com.google.api.client.util.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Configuration
//public class CorsConfig {
//
//    @Value("${cors.allowed-origins}")
//    private String originsProperty;
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        List<String> origins = Arrays.stream(originsProperty.split(","))
//                .map(String::trim)
//                .toList();
//
//        CorsConfiguration cfg = new CorsConfiguration();
//        cfg.setAllowedOriginPatterns(origins);
//        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        cfg.setAllowedHeaders(List.of("*"));
//        cfg.setAllowCredentials(true);
//        cfg.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
//        src.registerCorsConfiguration("/**", cfg);
//        return src;
//    }
//}
//
