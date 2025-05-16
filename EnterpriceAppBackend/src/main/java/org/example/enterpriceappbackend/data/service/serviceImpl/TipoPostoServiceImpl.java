package org.example.enterpriceappbackend.data.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.TipoPosto;
import org.example.enterpriceappbackend.data.repository.EventoRepository;
import org.example.enterpriceappbackend.data.repository.FeaturesRepository;
import org.example.enterpriceappbackend.data.repository.TipoPostoRepository;
import org.example.enterpriceappbackend.dto.TipoPostoDTO;
import org.example.enterpriceappbackend.data.service.TipoPostoService;
import org.example.enterpriceappbackend.exceptions.NotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TipoPostoServiceImpl implements TipoPostoService {

    private final TipoPostoRepository tipoPostoRepository;
    private final EventoRepository eventoRepository;
    private final FeaturesRepository featuresRepository;

    @Override
    @Transactional
    public TipoPostoDTO createTipoPosto(TipoPostoDTO dto) {
        TipoPosto tipoPosto = new TipoPosto();
        tipoPosto.setNome(dto.getNome());
        tipoPosto.setPrezzo(dto.getPrezzo());
        tipoPosto.setPostiDisponibili(dto.getPostiDisponibili());

        tipoPosto.setEvento(eventoRepository.findById(dto.getEventoId())
                .orElseThrow(() -> new NotFound("Evento non trovato")));

        tipoPosto.setFeatures(featuresRepository.findById(dto.getFeaturesId())
                .orElseThrow(() -> new NotFound("Features non trovate")));

        TipoPosto saved = tipoPostoRepository.save(tipoPosto);
        return convertToDTO(saved);
    }

    @Override
    public TipoPostoDTO getTipoPostoById(Long id) {
        TipoPosto tipoPosto = tipoPostoRepository.findById(id)
                .orElseThrow(() -> new NotFound("Tipo posto non trovato"));
        return convertToDTO(tipoPosto);
    }

    @Override
    public List<TipoPostoDTO> getTipiPostoByEvento(Long eventoId) {
        return tipoPostoRepository.findByEventoId(eventoId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getTotalPostiByEvento(Long eventoId) {
        return tipoPostoRepository.sumPostiByEvento(eventoId)
                .orElseThrow(() -> new NotFound("Evento non trovato"));
    }

    @Override
    public TipoPostoDTO getTipoPostoByPrezzo(Double prezzo) {
        TipoPosto tipoPosto = tipoPostoRepository.findByPrezzo(prezzo)
                .orElseThrow(() -> new NotFound("Tipo posto non trovato per il prezzo specificato"));
        return convertToDTO(tipoPosto);
    }

    private TipoPostoDTO convertToDTO(TipoPosto tipoPosto) {
        TipoPostoDTO dto = new TipoPostoDTO();
        dto.setId(tipoPosto.getId());
        dto.setNome(tipoPosto.getNome());
        dto.setPrezzo(tipoPosto.getPrezzo());
        dto.setPostiDisponibili(tipoPosto.getPostiDisponibili());
        dto.setEventoId(tipoPosto.getEvento().getId());
        dto.setFeaturesId(tipoPosto.getFeatures().getId());
        return dto;
    }
}