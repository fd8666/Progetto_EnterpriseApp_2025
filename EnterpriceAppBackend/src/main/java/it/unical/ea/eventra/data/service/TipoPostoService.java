package it.unical.ea.eventra.data.service;

import it.unical.ea.eventra.dto.TipoPostoDTO;
import java.util.List;

public interface TipoPostoService {
    TipoPostoDTO createTipoPosto(TipoPostoDTO dto);
    TipoPostoDTO getTipoPostoById(Long id);
    List<TipoPostoDTO> getTipiPostoByEvento(Long eventoId);
    Integer getTotalPostiByEvento(Long eventoId);
    TipoPostoDTO getTipoPostoByPrezzo(Double prezzo);
}