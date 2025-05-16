
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(

                                "/api/utente/login",
                                "/api/utente/register",
                                "api/utente/aggiornaPassword",
                                "/api/utente/allUtenti",
                                "/api/utente/elimina/{id}",
                                "/api/utente/{id}"
                        ).permitAll()
                        .requestMatchers(
                                "/api/tipi-posto/{id}",
                                "api/wishlist/condiviseCon/{id}",
                                "/api/ordine/aggiungi",
                                "/api/ordine/aggiorna/",
                                "api/ordine/elimina/",
                                "/api/wishlist/condivisa/create",
                                "/api/wishlist/condivisa/remove/1/1",
                                "/api/wishlist/condivisa/remove-by-wishlist/1",
                                "/api/wishlist/condivisa/by-wishlist/1",
                                "/api/tipi-posto/create",
                                "/api/tipi-posto/evento/1",
                                "/api/tipi-posto/total-posti/1",
                                "/api/tipi-posto/by-prezzo/50.00",
                                "/api/biglietto/1",
                                "/api/biglietto/1/spettatore",
                                "/api/biglietto/tipo-posto/1",
                                "/api/biglietto/1/prezzo",
                                "/api/biglietto/utente/1",
                                "/api/biglietto/evento/1",
                                "/api/biglietto/create"
                                ).hasRole("USER").anyRequest().authenticated()

                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
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
