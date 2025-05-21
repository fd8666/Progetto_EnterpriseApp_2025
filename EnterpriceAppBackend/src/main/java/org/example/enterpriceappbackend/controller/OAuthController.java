package org.example.enterpriceappbackend.controller;


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
public class OAuthController {

    private final UtenteService utenteService;

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
