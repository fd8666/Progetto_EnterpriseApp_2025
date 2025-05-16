package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.dto.TipoPostoDTO;
import java.util.List;

public interface TipoPostoService {
    TipoPostoDTO createTipoPosto(TipoPostoDTO dto);
    TipoPostoDTO getTipoPostoById(Long id);
    List<TipoPostoDTO> getTipiPostoByEvento(Long eventoId);
    Integer getTotalPostiByEvento(Long eventoId);
    TipoPostoDTO getTipoPostoByPrezzo(Double prezzo);
}