package org.example.enterpriceappbackend.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.service.UtenteService;
import org.example.enterpriceappbackend.dto.UtenteDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Api(value = "OAuth2 Login", description = "Endpoint per la gestione del login tramite Google", tags = {"OAuth2"})
public class OAuthController {

    private final UtenteService utenteService;

    @ApiOperation(
            value = "Login con Google OAuth2",
            notes = "Endpoint invocato automaticamente da Spring Security dopo un login Google OAuth2 andato a buon fine. "
                    + "Restituisce un messaggio di benvenuto con il nome dell'utente autenticato e salva/aggiorna l'utente nel database."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Login completato con successo"),
            @ApiResponse(code = 401, message = "Non autorizzato. Accesso negato o token non valido"),
            @ApiResponse(code = 500, message = "Errore interno del server durante l'elaborazione del login")
    })
    @GetMapping("/oauth/success")
    public ResponseEntity<UtenteDTO> getLoginInfo(OAuth2AuthenticationToken authentication) {
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
        String email = (String) attributes.get("email");
        Utente utente = utenteService.getOrCreateUser(email, attributes);

        // Converti a DTO
        UtenteDTO utenteDTO = new UtenteDTO();
        utenteDTO.setId(utente.getId());
        utenteDTO.setNome(utente.getNome());
        utenteDTO.setCognome(utente.getCognome());
        utenteDTO.setEmail(utente.getEmail());
        utenteDTO.setNumerotelefono(utente.getNumeroTelefono());
        utenteDTO.setRole(utente.getRole().name());

        return ResponseEntity.ok(utenteDTO);
    }
}
