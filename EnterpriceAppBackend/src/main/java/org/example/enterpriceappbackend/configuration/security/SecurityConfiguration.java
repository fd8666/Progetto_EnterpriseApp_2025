
package org.example.enterpriceappbackend.configuration.security;


import lombok.RequiredArgsConstructor;

import org.example.enterpriceappbackend.data.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Autowired
    @Lazy
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserServi;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/utente/allUtenti").hasRole("ADMIN")
                        .requestMatchers("/api/utente/elimina/**").hasRole("ADMIN")
                        .requestMatchers("/api/evento/create").hasRole("ADMIN")
                        .requestMatchers("/api/tipi-posto/create").hasRole("ADMIN")
                        .requestMatchers("/api/evento/organizzatore/{organizzatoreId}").hasRole("ADMIN")
                        .requestMatchers("/api/evento/update/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/evento/delete/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/biglietto/evento/{eventoId}").hasRole("ADMIN")
                        .requestMatchers("/api/features/save").hasRole("ADMIN")
                        .requestMatchers("/api/strutture/**").hasRole("ADMIN")
                        .requestMatchers("/api/tag-categoria").hasRole("ADMIN")
                        .requestMatchers("/organizzatore/{organizzatoreId}").hasRole("ADMIN")
                        .requestMatchers("/api/zone/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/api/utente/login",
                                "/api/utente/register",
                                "/api/utente/passwordDimenticata",
                                "/oauth2/**",
                                "/login/**",
                                "/oauth/success",
                                "/api/ordine/aggiungi"
                        ).permitAll()
                        .requestMatchers("/api/**").hasRole("USER")
                        .requestMatchers("api/strutture/{id}/map").hasRole("USER")
                        .requestMatchers("api/strutture/evento/{eventoId}").hasRole("USER")
                        .requestMatchers("api/strutture/evento/{id}/utente").hasRole("USER")
                        .anyRequest().denyAll()
                )
                // Permettiamo sessione se richiesta (per gestire OAuth2), seno blocca la richiesta
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/oauth/success", true)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserServi)
                        )
                )
                .build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public UserDetailsService userDetailsService(UtenteRepository utenteRepository) {
        return email -> (UserDetails) utenteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + email));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration Configuration) throws Exception {
        return Configuration.getAuthenticationManager();
    }
}
