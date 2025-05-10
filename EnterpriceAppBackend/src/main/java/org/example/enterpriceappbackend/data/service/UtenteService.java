package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.dto.UtenteDTO;
import org.example.enterpriceappbackend.dto.UtenteLoginDTO;
import org.example.enterpriceappbackend.dto.UtenteRegistrazioneDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UtenteService {

    void save (Utente utente);

    ResponseEntity<?> RegistrazioneUtente(UtenteRegistrazioneDTO utenteRegistrazione) throws Exception;

    ResponseEntity<?> AutenticazioneUtente(UtenteLoginDTO utenteLoginDTO) throws Exception;

    UtenteDTO getUtenteByToken(String token) throws Exception;

    void AggiornaPassword(String token, String newPassword) throws Exception;

    List<Utente> getAllUtenti();

    void deleteUtente(Long id);

    void AggiornaLaPasswordTramiteEmail(String email);
}
