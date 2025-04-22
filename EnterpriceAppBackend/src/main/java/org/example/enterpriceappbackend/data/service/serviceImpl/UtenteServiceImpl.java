package org.example.enterpriceappbackend.data.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.configuration.security.TokenStore;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.repository.UtenteRepository;
import org.example.enterpriceappbackend.data.service.UtenteService;
import org.example.enterpriceappbackend.dto.UtenteDTO;
import org.example.enterpriceappbackend.dto.UtenteRegistrazioneDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class UtenteServiceImpl implements UtenteService {

    private final UtenteRepository utenteRepository;
    private final TokenStore tokenStore;

    @Override
    public void save(Utente utente) {
        utenteRepository.save(utente);
    }

    @Override
    public ResponseEntity<?> RegistrazioneUtente(UtenteRegistrazioneDTO utenteRegistrazione) throws Exception {
        return null;
    }

    @Override
    public UtenteDTO getUtenteByToken(String token) throws Exception {
        return null;
    }

    @Override
    public UtenteDTO createUtente(UtenteDTO utenteDTO) { return null;}

    @Override
    public UtenteDTO getUtenteById(Long id) {return null;}

    @Override
    public List<UtenteDTO> getAllUtenti() {return List.of();}

    @Override
    public void AggiornaPassword(String token, String newPassword) throws Exception {}

    @Override
    public UtenteDTO UpdateUtente(Long id, UtenteDTO utenteDTO) {return null;}

    @Override
    public void deleteUtente(Long id) {}

    @Override
    public void AggiornaLaPasswordTramiteEmail(String email) {}
}
