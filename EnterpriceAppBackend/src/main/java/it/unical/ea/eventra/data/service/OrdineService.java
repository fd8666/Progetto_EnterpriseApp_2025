package it.unical.ea.eventra.data.service;

import it.unical.ea.eventra.data.entity.Ordine;
import it.unical.ea.eventra.dto.OrdineDTO;

import java.util.List;

public interface OrdineService {

    Ordine findById(long id);

    List<OrdineDTO> findByProprietario(Long proprietarioId);

    void save(OrdineDTO ordineDTO);

    OrdineDTO update(Long id, OrdineDTO ordineDTO);

    void delete(Long id);

    OrdineDTO aggiungiOrdine(OrdineDTO ordineDTO,Long idproprietario);

    OrdineDTO createOrdineWithPagamento(OrdineDTO ordinedto, Long eventoId, Long tipoPostoId, int quantita);

}
