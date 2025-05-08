package org.example.enterpriceappbackend.data.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Zona;
import org.example.enterpriceappbackend.data.repository.ZonaRepository;
import org.example.enterpriceappbackend.data.service.ZonaService;
import org.example.enterpriceappbackend.dto.ZonaInfoDTO;
import org.example.enterpriceappbackend.exceptions.BadRequest;
import org.example.enterpriceappbackend.exceptions.NotFound;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZonaServiceImpl implements ZonaService {

    private final ZonaRepository zonaRepository;

    @Override
    public Zona getById(Long id) {

        if (id == null || id <= 0) {
            throw new BadRequest("ID della zona non valido.");
        }
        return zonaRepository.findById(id)
                .orElseThrow(() -> new NotFound("Zona non trovata con ID: " + id));

    }

    @Override
    public List<ZonaInfoDTO> getZoneByStrutturaId(Long strutturaId) {

        if (strutturaId == null || strutturaId <= 0) {
            throw new BadRequest("ID non valido.");
        }
        List<Zona> zone = zonaRepository.findAllByStrutturaId(strutturaId);
        if (zone.isEmpty()) {
            throw new NotFound("Nessuna zona trovata per la struttura con ID " + strutturaId);
        }

        return zone.stream()
                .map(this::mapToDto)
                .toList();

    }

    private ZonaInfoDTO mapToDto(Zona zona) {

        ZonaInfoDTO dto = new ZonaInfoDTO();
        dto.setId(zona.getId());
        dto.setNome(zona.getNome());
        dto.setDescrizione(zona.getDescrizione());
        dto.setTotalePosti(zona.getTotalePosti() != null ? zona.getTotalePosti() : 0);
        return dto;

    }
}

