package org.example.enterpriceappbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.example.enterpriceappbackend.configuration.*;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.repository.UtenteRepository;
import org.example.enterpriceappbackend.data.service.JwtService;
import org.example.enterpriceappbackend.data.service.UtenteService;
import org.example.enterpriceappbackend.dto.UtenteDTO;
import org.example.enterpriceappbackend.dto.RequestAuthentication;
import org.example.enterpriceappbackend.dto.ResponseAuthentication;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping(path="/api/utente")
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UtenteController {

    private final UtenteService utenteservice;
    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UtenteRepository utenteRepository;


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody RequestAuthentication authentication) {
        try {
            Utente utente = utenteservice.AutenticazioneUtente(authentication);

            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    utente.getEmail(),
                    utente.getPassword(),
                    List.of()
            );

            String jwt = jwtService.generateToken(userDetails);
            System.out.println(jwt);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            ResponseAuthentication response = new ResponseAuthentication(jwt, refreshToken);

            return ResponseEntity.ok(new ApiResponseConfiguration<>(true, "Login effettuato con successo!", response));

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseConfiguration<>(false, "Utente non trovato!", null));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseConfiguration<>(false, "Credenziali non valide!", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseConfiguration<>(false, "Errore durante il login: " + e.getMessage(), null));
        }
    }


    @PostMapping("/register")
    public ApiResponseConfiguration<ResponseAuthentication> register(@Valid @RequestBody UtenteDTO utenteDTO) {
        try {
            if (utenteRepository.existsByEmail(utenteDTO.getEmail())) {
                throw new IllegalArgumentException("L'email è già in uso");
            }

            utenteservice.RegistrazioneUtente(utenteDTO);

            Utente user = utenteRepository.findByEmail(utenteDTO.getEmail()).orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

            String token = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            ResponseAuthentication responseAuthentication = new ResponseAuthentication(token, refreshToken);

            return new ApiResponseConfiguration<>(true, "Registrazione avvenuta con successo", responseAuthentication);

        } catch (IllegalArgumentException e) {
            return new ApiResponseConfiguration<>(false, "Errore: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponseConfiguration<>(false, "Errore durante la registrazione: " + e.getMessage(), null);
        }
    }


    @GetMapping("/allUtenti")
    public ResponseEntity<List<Utente>>getAllUtenti() {
        List<Utente> utenti = utenteservice.getAllUtenti();
        return ResponseEntity.ok(utenti);
    }


    @GetMapping("/{id}")
    public ApiResponseConfiguration<UtenteDTO> getUserData(@PathVariable @Min(1) Long id) {
        UtenteDTO user = utenteservice.getById(id);
        if (user!=null) {
            return new ApiResponseConfiguration<>(true, "Dati utente recuperati con successo", user);
        } else {
            return new ApiResponseConfiguration<>(false, "Utente non valido o assente", null);
        }
    }

    //ok
    @PostMapping("/aggiornaPassword")
    public ApiResponseConfiguration<String> aggiornaPassword(@Valid @RequestParam("nuovaPassword") String nuovaPassword, HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new ApiResponseConfiguration<>(false, "Token assente o malformato", null);
            }

            String token1 = authHeader.substring(7);

            if (!jwtService.isTokenValid(token1)) {
                return new ApiResponseConfiguration<>(false, "Token non valido o scaduto", null);
            }

            String token2 = jwtService.extractUsername(token1);
            utenteservice.AggiornaPassword(token2, nuovaPassword);

            return new ApiResponseConfiguration<>(true, "Password aggiornata con successo", "Password aggiornata");

        } catch (Exception e) {
            return new ApiResponseConfiguration<>(false, "Errore durante l'aggiornamento della password: " + e.getMessage(), null);
        }
    }

    // corretto
    @PostMapping("/refresh")
    public ResponseEntity<ResponseAuthentication> refresh( @RequestParam("refreshtoken") String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String newToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new ResponseAuthentication(newToken, refreshToken));
    }



    @DeleteMapping("/elimina/{id}")
    public ResponseEntity<?> eliminaUtente(@PathVariable Long id) {
        try {
            utenteservice.deleteUtente(id);
            return ResponseEntity.ok("Utente eliminato con successo.");
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utente non trovato.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'eliminazione dell'utente.");
        }
    }


}
