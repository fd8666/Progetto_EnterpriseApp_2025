package it.unical.ea.eventra.data.service;

import it.unical.ea.eventra.dto.FeaturesDTO;

public interface FeaturesService {

    FeaturesDTO create(FeaturesDTO dto);
    FeaturesDTO update(Long id, FeaturesDTO dto);

    FeaturesDTO getByTipoPostoId(Long tipoPostoId);
}
