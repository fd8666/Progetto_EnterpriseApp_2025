package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.data.entity.Features;
import org.example.enterpriceappbackend.dto.FeaturesDTO;

import java.util.List;

public interface FeaturesService {

    FeaturesDTO create(FeaturesDTO dto);
    FeaturesDTO update(Long id, FeaturesDTO dto);

    FeaturesDTO getByTipoPostoId(Long tipoPostoId);
}
