package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.data.entity.TipoPosto;
import org.example.enterpriceappbackend.dto.TipoPostoDTO;

import java.util.List;

public interface TipoPostoService {
    TipoPostoDTO save(TipoPostoDTO dto);
    List<TipoPostoDTO> findByEventoId(Long eventoId);
    void delete(Long id);

}
