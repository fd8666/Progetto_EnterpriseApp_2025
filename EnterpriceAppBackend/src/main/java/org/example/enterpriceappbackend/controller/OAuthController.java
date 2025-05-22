package org.example.enterpriceappbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "OAuth", description = "Gestione login OAuth e recupero informazioni utente")
public class OAuthController {

    private final UtenteService utenteService;

    @Operation(
            summary = "Login con OAuth2",
            description = "Endpoint chiamato automaticamente dopo un login OAuth2 riuscito. Recupera le informazioni dell’utente da Google (o altro provider) e crea un nuovo utente nel sistema se non esiste già."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utente autenticato con successo"),
            @ApiResponse(responseCode = "401", description = "Autenticazione fallita o token non valido"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/oauth/success")
    public ResponseEntity<UtenteDTO> getLoginInfo(OAuth2AuthenticationToken authentication) {
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
        String email = (String) attributes.get("email");
        Utente utente = utenteService.getOrCreateUser(email, attributes);

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
