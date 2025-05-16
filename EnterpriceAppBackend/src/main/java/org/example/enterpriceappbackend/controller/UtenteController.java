package org.example.enterpriceappbackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping(path="/api/utente")
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Api(value = "Utente API", description = "Operazioni relative alla gestione degli Utenti", tags = {"Utenti"})
public class UtenteController {

    private final UtenteService utenteservice;
    private final JwtService jwtService;
    private final UtenteRepository utenteRepository;

    @ApiOperation(value = "Login utente")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Login effettuato con successo!"),
            @ApiResponse(code = 401, message = "Credenziali non valide!"),
            @ApiResponse(code = 404, message = "Utente non trovato!")
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

    @ApiOperation(value = "Registrazione di un utente")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Registrazione avvenuta con successo!"),
            @ApiResponse(code = 400, message = "Dati non validi!"),
            @ApiResponse(code = 404, message = "Utente non trovato!")
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

            ResponseAuthentication responseAuthentication = new ResponseAuthentication(token, refreshToken);

            return new ApiResponseConfiguration<>(true, "Registrazione avvenuta con successo", responseAuthentication);

        } catch (IllegalArgumentException e) {
            return new ApiResponseConfiguration<>(false, "Errore: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponseConfiguration<>(false, "Errore durante la registrazione: " + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "dare i dati di tutti gli utenti")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "dati richiesti con successo!"),
            @ApiResponse(code = 400, message = "Dati non validi!"),
            @ApiResponse(code = 404, message = "Utente non trovato!")
    })
    @GetMapping("/allUtenti")
    public ResponseEntity<List<Utente>>getAllUtenti() {
        List<Utente> utenti = utenteservice.getAllUtenti();
        return ResponseEntity.ok(utenti);
    }

    @ApiOperation(value = "dare i dati di un utente")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "dati richiesti con successo!"),
            @ApiResponse(code = 400, message = "Dati non validi!"),
            @ApiResponse(code = 404, message = "Utente non trovato!")
    })
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
    @ApiOperation(value = "Aggiorna la password di un utente")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Password aggiornata con successo!"),
            @ApiResponse(code = 400, message = "Dati non validi!"),
            @ApiResponse(code = 401, message = "Token non valido o assente!"),
            @ApiResponse(code = 500, message = "Errore interno del server!")
    })
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


    /*google*/

    // ok
    @ApiOperation(value = "Elimina un utente tramite il suo id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Utente eliminato con successo!"),
            @ApiResponse(code = 400, message = "Dati non validi!"),
            @ApiResponse(code = 404, message = "Utente non trovato!")
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

    @ApiOperation(value = "recupera la password di un utente")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Password recuperata con successo!"),
            @ApiResponse(code = 400, message = "Dati non validi!"),
            @ApiResponse(code = 404, message = "Utente non trovato!")
    })
    @PostMapping("/passwordDimenticata")
    public ResponseEntity<String> passwordDimenticata(@RequestParam @Email @NotBlank String email){
        String password=utenteservice.AggiornaLaPasswordTramiteEmail(email);
        try{

            return new ResponseEntity<>("EMAIL INVIATA CORRETTAMENTE "+ password, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(password);
            return new ResponseEntity<>("ERRORE DURANTE L'INVIO DELLA EMAIL ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
