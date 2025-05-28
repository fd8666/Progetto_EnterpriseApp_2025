package it.unical.ea.eventra.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import it.unical.ea.eventra.data.entity.Zona;
import it.unical.ea.eventra.data.service.ZonaService;
import it.unical.ea.eventra.dto.ZonaInfoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/zone")
@RequiredArgsConstructor
@Tag(name = "Zona", description = "Operazioni relative alla gestione delle zone")
public class ZonaController {

    private final ZonaService zonaService;

    @Operation(
            summary = "Ottieni zona per ID",
            description = "Recupera una zona completa utilizzando l'ID specificato.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zona trovata"),
            @ApiResponse(responseCode = "404", description = "Zona non trovata con l'ID specificato"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/{zonaId}")
    public ResponseEntity<Zona> getZonaById(@PathVariable Long zonaId) {
        Zona zona = zonaService.getById(zonaId);
        return ResponseEntity.ok(zona);
    }

    @Operation(
            summary = "Ottieni tutte le zone per una struttura",
            description = "Restituisce una lista di oggetti ZonaInfoDTO relativi a tutte le zone associate a una struttura specificata tramite ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zone trovate"),
            @ApiResponse(responseCode = "404", description = "Nessuna zona trovata per la struttura specificata"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/struttura/{strutturaId}")
    public ResponseEntity<List<ZonaInfoDTO>> getZoneByStruttura(@PathVariable Long strutturaId) {
        List<ZonaInfoDTO> zone = zonaService.getZoneByStrutturaId(strutturaId);
        return ResponseEntity.ok(zone);
    }

}
