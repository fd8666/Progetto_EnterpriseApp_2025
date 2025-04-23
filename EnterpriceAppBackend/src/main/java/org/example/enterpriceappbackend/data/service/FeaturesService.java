package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.data.entity.Features;
import org.example.enterpriceappbackend.dto.FeaturesDTO;

import java.util.List;

public interface FeaturesService {
    FeaturesDTO save(FeaturesDTO dto);
    List<FeaturesDTO> findByZonaId(Long zonaId);
    void delete(Long id);


}
