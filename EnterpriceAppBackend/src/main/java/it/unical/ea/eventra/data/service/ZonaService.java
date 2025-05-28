package it.unical.ea.eventra.data.service;

import it.unical.ea.eventra.data.entity.Zona;
import it.unical.ea.eventra.dto.ZonaInfoDTO;

import java.util.List;

public interface ZonaService {

    Zona getById(Long id);
    List<ZonaInfoDTO> getZoneByStrutturaId(Long strutturaId);

}
