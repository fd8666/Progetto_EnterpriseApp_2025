package it.unical.ea.eventra.data.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import it.unical.ea.eventra.data.entity.Zona;
import it.unical.ea.eventra.data.repository.ZonaRepository;
import it.unical.ea.eventra.data.service.ZonaService;
import it.unical.ea.eventra.dto.ZonaInfoDTO;
import it.unical.ea.eventra.exception.BadRequest;
import it.unical.ea.eventra.exception.NotFound;
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

