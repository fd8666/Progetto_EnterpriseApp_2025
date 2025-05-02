package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.dto.PagamentoDTO;
import org.example.enterpriceappbackend.dto.PagamentoRequestDTO;
import org.example.enterpriceappbackend.dto.PagamentoResponseDTO;

import java.util.List;

public interface PagamentoService {

    List<PagamentoDTO> findAll();

    PagamentoDTO findById(Long id);

    List<PagamentoDTO> findByUtenteId(Long UtenteId);

    PagamentoDTO save(PagamentoDTO pagamentoDTO);

    PagamentoDTO update(Long id, PagamentoDTO pagamentoDTO);

    void deleteById(Long id);

    PagamentoResponseDTO processpayment(PagamentoRequestDTO richiesta);
}
