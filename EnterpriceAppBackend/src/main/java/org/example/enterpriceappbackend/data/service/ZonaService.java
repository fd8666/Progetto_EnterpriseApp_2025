package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.data.entity.Zona;
import org.example.enterpriceappbackend.dto.ZonaInfoDTO;

import java.util.List;

public interface ZonaService {

    Zona getById(Long id);
    List<ZonaInfoDTO> getZoneByStrutturaId(Long strutturaId);

}
