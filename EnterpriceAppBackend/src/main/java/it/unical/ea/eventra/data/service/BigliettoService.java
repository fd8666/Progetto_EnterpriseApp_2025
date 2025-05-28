package it.unical.ea.eventra.data.service;

import it.unical.ea.eventra.dto.BigliettoCreateDTO;
import it.unical.ea.eventra.dto.BigliettoEditSpettatoreDTO;
import it.unical.ea.eventra.dto.BigliettoInfoDTO;

import java.util.List;

public interface BigliettoService {
    BigliettoInfoDTO findById(Long id);
    List<BigliettoInfoDTO> findByTipoPosto(Long tipoPostoId);
    List<BigliettoInfoDTO> findByEvento(Long eventoId);
    BigliettoInfoDTO createBiglietto(BigliettoCreateDTO bigliettoCreateDTO);
    List<BigliettoInfoDTO> findByUtente(Long utenteId);
    BigliettoInfoDTO updateSpettatore(Long id, BigliettoEditSpettatoreDTO bigliettoEditSpettatoreDTO);
    void deleteBiglietto(Long id);
    Double getPrezzoBiglietto(Long id);
    void checkBigliettiScaduti();
}