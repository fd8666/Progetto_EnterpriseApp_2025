package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.dto.UtenteDTO;
import org.example.enterpriceappbackend.dto.UtenteRegistrazioneDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UtenteService {

    public void save (Utente utente);

    ResponseEntity<?> RegistrazioneUtente(UtenteRegistrazioneDTO utenteRegistrazione) throws Exception;

    UtenteDTO getUtenteByToken(String token) throws Exception;

    UtenteDTO createUtente(UtenteDTO utenteDTO);

    UtenteDTO getUtenteById(Long id);

    List<UtenteDTO> getAllUtenti();

    void AggiornaPassword(String token, String newPassword) throws Exception;

    UtenteDTO UpdateUtente(Long id,UtenteDTO utenteDTO);

    void deleteUtente(Long id);

    void AggiornaLaPasswordTramiteEmail(String email);
}
