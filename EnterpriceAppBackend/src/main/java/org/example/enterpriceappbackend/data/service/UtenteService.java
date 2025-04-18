package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.dto.UtenteDTO;

import java.util.List;

public interface UtenteService {
    UtenteDTO createUtente(UtenteDTO utenteDTO);
    UtenteDTO getUtenteById(Long id);
    List<UtenteDTO> getAllUtenti();
    UtenteDTO UpdateUtente(Long id,UtenteDTO utenteDTO);
    void deleteUtente(Long id);

}
