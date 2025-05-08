package org.example.enterpriceappbackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Zona;
import org.example.enterpriceappbackend.data.service.ZonaService;
import org.example.enterpriceappbackend.dto.ZonaInfoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*",allowedHeaders = "*")
@RequestMapping("/zone")
@RequiredArgsConstructor
@Api(value = "Zone API", description = "Operazioni relative alla visualizzazione delle zone", tags = {"Zone"})
public class ZonaController {

    private final ZonaService zonaService;

    @ApiOperation(
            value = "Ottieni zona per ID",
            notes = "Restituisce i dettagli di una singola zona identificata dal suo ID.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Zona trovata"),
            @ApiResponse(code = 404, message = "Zona non trovata"),
            @ApiResponse(code = 400, message = "Richiesta non valida"),
            @ApiResponse(code = 401, message = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/{zonaId}")
    public ResponseEntity<Zona> getZonaById(@PathVariable Long zonaId) {
        Zona zona = zonaService.getById(zonaId);
        return ResponseEntity.ok(zona);
    }

    @ApiOperation(
            value = "Ottieni zone tramite struttura",
            notes = "Restituisce la lista di tutte le zone associate a una struttura specifica a partire dal suo ID.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Zone trovate"),
            @ApiResponse(code = 404, message = "Nessuna zona trovata per la struttura specificata"),
            @ApiResponse(code = 400, message = "Richiesta o ID struttura non valida/o"),
            @ApiResponse(code = 401, message = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/struttura/{strutturaId}")
    public ResponseEntity<List<ZonaInfoDTO>> getZoneByStruttura(@PathVariable Long strutturaId) {
        List<ZonaInfoDTO> zone = zonaService.getZoneByStrutturaId(strutturaId);
        return ResponseEntity.ok(zone);
    }

}
