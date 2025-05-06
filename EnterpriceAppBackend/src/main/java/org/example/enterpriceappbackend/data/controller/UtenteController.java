package org.example.enterpriceappbackend.data.controller;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.configuration.ApiResponse;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.service.UtenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@RequiredArgsConstructor
@RequestMapping(path="/api/utente")
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UtenteController {
    private final UtenteService utenteservice;

    /* aggiugnere autenticazione, registrazionedare, se determinati utenti possono accedere a determinati dati, google, aggiornare la password, facebook*/

    @PostMapping("password-dimenticata")
    public ResponseEntity<String> passwordDimenticata(@RequestParam("email") String email) {
        try{
            utenteservice.AggiornaLaPasswordTramiteEmail(email);
            return new ResponseEntity<>("EMAIL INVIATA CORRETTAMENTE ", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("ERRORE DURANTE L'INVIO DELLA EMAIL ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
