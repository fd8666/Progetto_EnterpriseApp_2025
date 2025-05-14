package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.dto.UtenteDTO;
import org.example.enterpriceappbackend.dto.RequestAuthentication;

import java.util.List;

public interface UtenteService {

    void save (Utente utente);

    void RegistrazioneUtente(UtenteDTO utenteDTO) throws Exception;

    Utente AutenticazioneUtente(RequestAuthentication utenteLoginDTO) throws Exception;

    void AggiornaPassword(String token, String newPassword) throws Exception;

    List<Utente> getAllUtenti();

    UtenteDTO getById(Long id);

    void deleteUtente(Long id);

    Utente getOrCreateUser(String email);

    String AggiornaLaPasswordTramiteEmail(String email);
}
