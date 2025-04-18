package org.example.enterpriceappbackend.data.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.repository.UtenteRepository;
import org.example.enterpriceappbackend.data.service.UtenteService;
import org.example.enterpriceappbackend.dto.UtenteDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class UtenteServiceImpl implements UtenteService {

    private final UtenteRepository utenteRepository;

    @Override
    public UtenteDTO createUtente(UtenteDTO utenteDTO) { return null;}

    @Override
    public UtenteDTO getUtenteById(Long id) {return null;}

    @Override
    public List<UtenteDTO> getAllUtenti() {return List.of();}

    @Override
    public UtenteDTO UpdateUtente(Long id, UtenteDTO utenteDTO) {return null;}

    @Override
    public void deleteUtente(Long id) {

    }
}
