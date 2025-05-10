package org.example.enterpriceappbackend.configuration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.example.enterpriceappbackend.data.service.serviceImpl.UtenteServiceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestFilter extends OncePerRequestFilter {

    private final TokenStore tokenStore;
    private final UtenteServiceImpl utenteService;

    public RequestFilter(TokenStore tokenStore, UtenteServiceImpl utenteService) {
        this.tokenStore = tokenStore;
        this.utenteService = utenteService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res, @NonNull FilterChain chain) throws ServletException, IOException {
        String token = tokenStore.estraiTokenDaRichiesta(req);
        System.out.println("TOKEN: " + token);

        if (token != null && !"invalid".equals(token)){
            try{
                String email = tokenStore.getEmailDaToken(token);
                UserDetails utente = utenteService.loadUtenteByUsername(email);
                if (utente != null){
                    UsernamePasswordAuthenticationToken autenticato = new UsernamePasswordAuthenticationToken(utente, null, utente.getAuthorities());
                    autenticato.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(autenticato);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        chain.doFilter(req, res);
    }
}
