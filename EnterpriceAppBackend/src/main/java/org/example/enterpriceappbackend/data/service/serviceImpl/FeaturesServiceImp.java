package org.example.enterpriceappbackend.data.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.service.FeaturesService;
import org.example.enterpriceappbackend.dto.FeaturesDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FeaturesServiceImp implements FeaturesService {
    @Override
    public FeaturesDTO save(FeaturesDTO dto) {
        return null;
    }

    @Override
    public List<FeaturesDTO> findByZonaId(Long zonaId) {
        return List.of();
    }

    @Override
    public void delete(Long id) {

    }
}
