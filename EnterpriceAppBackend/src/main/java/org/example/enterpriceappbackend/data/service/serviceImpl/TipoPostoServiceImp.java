package org.example.enterpriceappbackend.data.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.TipoPosto;
import org.example.enterpriceappbackend.data.service.TipoPostoService;
import org.example.enterpriceappbackend.dto.TipoPostoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TipoPostoServiceImp implements TipoPostoService {


    @Override
    public TipoPostoDTO save(TipoPostoDTO dto) {
        return null;
    }

    @Override
    public List<TipoPostoDTO> findByEventoId(Long eventoId) {
        return List.of();
    }

    @Override
    public void delete(Long id) {

    }
}
