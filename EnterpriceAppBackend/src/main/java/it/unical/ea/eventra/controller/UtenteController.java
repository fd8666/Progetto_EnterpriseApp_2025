package it.unical.ea.eventra.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import it.unical.ea.eventra.conf.ApiResponseConfiguration;
import it.unical.ea.eventra.data.entity.Utente;
import it.unical.ea.eventra.data.repository.UtenteRepository;
import it.unical.ea.eventra.core.JwtService;
import it.unical.ea.eventra.data.service.UtenteService;
import it.unical.ea.eventra.dto.RequestAuthentication;
import it.unical.ea.eventra.dto.ResponseAuthentication;
import it.unical.ea.eventra.dto.UtenteDTO;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping(path = "/api/utente")
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Utente", description = "API per la gestione degli utenti e autenticazione")
public class UtenteController {

    private final UtenteService utenteservice;
    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UtenteRepository utenteRepository;

    @Operation(
            summary = "Login utente",
            description = "Effettua il login con email e password, restituisce JWT e refresh token.")

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login effettuato con successo"),
            @ApiResponse(responseCode = "401", description = "Credenziali non valide"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
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
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            ResponseAuthentication response = new ResponseAuthentication(jwt, refreshToken,utente.getId());

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

    @Operation(
            summary = "Registrazione utente",
            description = "Registra un nuovo utente e restituisce JWT e refresh token.")

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registrazione avvenuta con successo"),
            @ApiResponse(responseCode = "400", description = "Email già in uso o dati non validi"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
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

            ResponseAuthentication responseAuthentication = new ResponseAuthentication(token, refreshToken,utenteDTO.getId());

            return new ApiResponseConfiguration<>(true, "Registrazione avvenuta con successo", responseAuthentication);

        } catch (IllegalArgumentException e) {
            return new ApiResponseConfiguration<>(false, "Errore: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponseConfiguration<>(false, "Errore durante la registrazione: " + e.getMessage(), null);
        }
    }

    @Operation(
            summary = "Recupera tutti gli utenti",
            description = "Restituisce la lista completa degli utenti registrati.")

    @ApiResponse(responseCode = "200", description = "Lista utenti restituita con successo")
    @GetMapping("/allUtenti")
    public ResponseEntity<List<Utente>> getAllUtenti() {
        List<Utente> utenti = utenteservice.getAllUtenti();
        return ResponseEntity.ok(utenti);
    }

    @Operation(
            summary = "Recupera dati utente per ID",
            description = "Restituisce i dati dell'utente dato il suo ID.")

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dati utente recuperati con successo"),
            @ApiResponse(responseCode = "404", description = "Utente non valido o assente")
    })
    @GetMapping("/{id}")
    public ApiResponseConfiguration<UtenteDTO> getUserData(@PathVariable @Min(1) Long id) {
        UtenteDTO user = utenteservice.getById(id);
        if (user != null) {
            return new ApiResponseConfiguration<>(true, "Dati utente recuperati con successo", user);
        } else {
            return new ApiResponseConfiguration<>(false, "Utente non valido o assente", null);
        }
    }

    @Operation(
            summary = "Aggiorna password utente",
            description = "Aggiorna la password dell'utente autenticato usando il token JWT.")

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password aggiornata con successo"),
            @ApiResponse(responseCode = "400", description = "Token assente o malformato"),
            @ApiResponse(responseCode = "401", description = "Token non valido o scaduto"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
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

    @Operation(
            summary = "Aggiorna token JWT con refresh token",
            description = "Restituisce un nuovo JWT valido utilizzando un refresh token valido.")

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token aggiornato con successo"),
            @ApiResponse(responseCode = "403", description = "Refresh token non valido o scaduto")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ResponseAuthentication> refresh(@RequestParam("refreshtoken") String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String newToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new ResponseAuthentication(newToken, refreshToken,(long)1));
    }

    @Operation(
            summary = "Elimina un utente per ID",
            description = "Elimina un utente dato il suo ID. Ritorna messaggi appropriati se utente non trovato o errore.")

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utente eliminato con successo"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
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
