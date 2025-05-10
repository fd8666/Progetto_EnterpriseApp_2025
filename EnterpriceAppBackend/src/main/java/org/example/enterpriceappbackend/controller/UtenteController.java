package org.example.enterpriceappbackend.controller;

import com.nimbusds.jose.JOSEException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.example.enterpriceappbackend.configuration.*;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.configuration.security.TokenStore;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.service.UtenteService;
import org.example.enterpriceappbackend.dto.UtenteDTO;
import org.example.enterpriceappbackend.dto.UtenteRegistrazioneDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@RequestMapping(path="/api/utente")
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Api(value = "Utente API", description = "Operazioni relative alla gestione degli Utenti", tags = {"Utenti"})
public class UtenteController {

    private final TokenStore tokenStore;

    @Autowired
    private final UtenteService utenteservice;
    private final AuthenticationManager authenticationManager;

    @ApiOperation(value = "Autenticazione di un utente")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Autenticazione avvenuta con successo!"),
            @ApiResponse(code = 400, message = "Dati non validi!"),
            @ApiResponse(code = 404, message = "Utente non trovato!")
    })
    @PostMapping("/Login")
    public  ApiResponseConfiguration<String> Login(@RequestParam("email") String email, @RequestParam("password") String password) throws JOSEException {
        // Autenticazione dell'utente con email e password
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        String token = tokenStore.creaToken(Map.of("email", email));

        // Restituzione della risposta con il token
        return new ApiResponseConfiguration<>(true, "autenticazione avvenuta con successo", token);
    }

    @ApiOperation(value = "Registrazione di un utente")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Registrazione avvenuta con successo!"),
            @ApiResponse(code = 400, message = "Dati non validi!"),
            @ApiResponse(code = 404, message = "Utente non trovato!")
    })
    @PostMapping("/registrazione")
    public ApiResponseConfiguration<String> registrazione(@RequestBody UtenteRegistrazioneDTO utenteRegistrazioneDTO) {
        try {
            ResponseEntity<?> response = utenteservice.RegistrazioneUtente(utenteRegistrazioneDTO);

            // Se la registrazione è andata a buon fine, crea il token per l'utente
            String email = utenteRegistrazioneDTO.getCredenzialiEmail(); // recupera l'email dal DTO
            String token = tokenStore.creaToken(Map.of("email", email));

            return new ApiResponseConfiguration<>(true, "Registrazione avvenuta con successo", token);

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
    @GetMapping("/utente")
    public ApiResponseConfiguration<UtenteDTO> getUserData(HttpServletRequest request) {
        UtenteDTO user = null;
        String token = tokenStore.estraiTokenDaRichiesta(request);
        try {
            if (token != null && !"invalid".equals(token)) {
                user = utenteservice.getUtenteByToken(token);
                return new ApiResponseConfiguration<>(true, "Dati utente recuperati con successo", user);
            } else {
                return new ApiResponseConfiguration<>(false, "Token non valido o assente", null);
            }
        } catch (Exception e) {
            return new ApiResponseConfiguration<>(false, "Errore nel recupero dei dati utente: " + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "Aggiorna la password di un utente")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Password aggiornata con successo!"),
            @ApiResponse(code = 400, message = "Dati non validi!"),
            @ApiResponse(code = 404, message = "Utente non trovato!")
    })
    @PostMapping("/updatePassword")
    public ApiResponseConfiguration<String> updatePassword(@RequestParam("newPassword") String newPassword, HttpServletRequest request) {
        String token = tokenStore.estraiTokenDaRichiesta(request);
        try {
            if (token != null && !"invalid".equals(token)) {
                utenteservice.AggiornaPassword(token, newPassword);
                return new ApiResponseConfiguration<>(true, "Password aggiornata con successo", "Password aggiornata");
            } else {
                return new ApiResponseConfiguration<>(false, "Token non valido o assente", null);
            }
        } catch (Exception e) {
            return new ApiResponseConfiguration<>(false, "Errore durante l'aggiornamento della password: " + e.getMessage(), null);
        }
    }


     /*google facebook*/

    @ApiOperation(value = "recupera la password di un utente")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Password recuperata con successo!"),
            @ApiResponse(code = 400, message = "Dati non validi!"),
            @ApiResponse(code = 404, message = "Utente non trovato!")
    })
    @PostMapping("/passwordDimenticata")
    public ResponseEntity<String> passwordDimenticata(@RequestParam("email") String email) {
        try{
            utenteservice.AggiornaLaPasswordTramiteEmail(email);
            return new ResponseEntity<>("EMAIL INVIATA CORRETTAMENTE ", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("ERRORE DURANTE L'INVIO DELLA EMAIL ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
