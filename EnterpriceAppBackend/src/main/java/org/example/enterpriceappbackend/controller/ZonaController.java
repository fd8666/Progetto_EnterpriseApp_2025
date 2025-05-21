package org.example.enterpriceappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Zona;
import org.example.enterpriceappbackend.data.service.ZonaService;
import org.example.enterpriceappbackend.dto.ZonaInfoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*",allowedHeaders = "*")
@RequestMapping("/api/zone")
@RequiredArgsConstructor
public class ZonaController {

    private final ZonaService zonaService;

    @GetMapping("/{zonaId}")
    public ResponseEntity<Zona> getZonaById(@PathVariable Long zonaId) {
        Zona zona = zonaService.getById(zonaId);
        return ResponseEntity.ok(zona);
    }

    @GetMapping("/struttura/{strutturaId}")
    public ResponseEntity<List<ZonaInfoDTO>> getZoneByStruttura(@PathVariable Long strutturaId) {
        List<ZonaInfoDTO> zone = zonaService.getZoneByStrutturaId(strutturaId);
        return ResponseEntity.ok(zone);
    }

}
