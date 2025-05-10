package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.data.entity.Ordine;
import org.example.enterpriceappbackend.dto.OrdineDTO;

import java.util.List;

public interface OrdineService {

    Ordine findById(long id);

    List<OrdineDTO> findByProprietario(Long proprietarioId);

    Long save(OrdineDTO ordineDTO);

    OrdineDTO update(Long id, OrdineDTO ordineDTO);

    void delete(Long id);

    OrdineDTO createOrdineWithPagamento(OrdineDTO ordinedto, Long eventoId, Long tipoPostoId, int quantita);

}
