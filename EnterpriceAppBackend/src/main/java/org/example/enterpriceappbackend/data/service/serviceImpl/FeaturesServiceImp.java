package org.example.enterpriceappbackend.data.service.serviceImpl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enterpriceappbackend.data.entity.Features;
import org.example.enterpriceappbackend.data.entity.TipoPosto;
import org.example.enterpriceappbackend.data.repository.FeaturesRepository;
import org.example.enterpriceappbackend.data.repository.TipoPostoRepository;
import org.example.enterpriceappbackend.data.service.FeaturesService;
import org.example.enterpriceappbackend.dto.FeaturesDTO;
import org.example.enterpriceappbackend.exceptions.BadRequest;
import org.example.enterpriceappbackend.exceptions.NotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeaturesServiceImp implements FeaturesService {

    private final FeaturesRepository featuresRepository;
    private final TipoPostoRepository tipoPostoRepository;


    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequest(" ID non valido ");
        }
    }

    @Override
    public FeaturesDTO create(FeaturesDTO dto) {

        Features entity = toEntity(dto);
        Features saved = featuresRepository.save(entity);
        log.info("Feature creata con ID {}", saved.getId());
        return toDTO(saved);

    }

    @Override
    public FeaturesDTO update(Long id, FeaturesDTO dto) {

        validateId(id);
        Features existing = featuresRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Features non trovata con ID " + id));

        existing.setZona(dto.getZona());
        existing.setFeatures(dto.getFeatures());
        existing.setTipoPosto(getTipoPostoById(dto.getTipoPostoId()));

        Features updated = featuresRepository.save(existing);
        log.info("Feature aggiornata con ID {}", updated.getId());
        return toDTO(updated);

    }

    //delete non serve perche 1 a 1 andiamo ad eliminare direttamente il tipoposto correlato, le features si possono solo modioficare

    @Override
    public FeaturesDTO getByTipoPostoId(Long tipoPostoId) {

        validateId(tipoPostoId);
        Features feature = featuresRepository.findByTipoPostoId(tipoPostoId)
                .orElseThrow(() -> new NotFound("Feature non trovata per TipoPosto con id " + tipoPostoId));
        return toDTO(feature);

    }

    private FeaturesDTO toDTO(Features features) {
        FeaturesDTO dto = new FeaturesDTO();
        dto.setId(features.getId());
        dto.setZona(features.getZona());
        dto.setFeatures(features.getFeatures());
        dto.setTipoPostoId(features.getTipoPosto().getId());
        return dto;
    }

    private Features toEntity(FeaturesDTO dto) {
        Features entity = new Features();
        entity.setZona(dto.getZona());
        entity.setFeatures(dto.getFeatures());
        entity.setTipoPosto(getTipoPostoById(dto.getTipoPostoId()));
        return entity;
    }

    private TipoPosto getTipoPostoById(Long id) {
        return tipoPostoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TipoPosto non trovato con ID " + id));
    }

}
