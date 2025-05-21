package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.dto.PagamentoDTO;

import java.util.List;

public interface PagamentoService {

    PagamentoDTO createPagamento(Long ordineId, PagamentoDTO pagamentoDTO);

    List<PagamentoDTO> findByUtenteId(Long UtenteId);

    PagamentoDTO create(PagamentoDTO pagamentoDTO);

    PagamentoDTO save(PagamentoDTO pagamentoDTO);

    PagamentoDTO update(Long id, PagamentoDTO pagamentoDTO);

    void deleteById(Long id);

}
