package org.example.enterpriceappbackend.controller;

import io.swagger.annotations.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.service.FeaturesService;
import org.example.enterpriceappbackend.dto.FeaturesDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/features")
@RequiredArgsConstructor
@Api(value = "Features API", description = "Operazioni relative alla visualizzazione e creazione delle features", tags = {"Features"})
public class FeaturesController {

    private final FeaturesService featuresService;

    @ApiOperation(
            value = "Ottieni una feature per TipoPosto",
            notes = "Restituisce una `FeatureDTO` associata a un `TipoPosto` tramite il suo ID."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Feature trovata con successo"),
            @ApiResponse(code = 400, message = "Richiesta o ID TipoPosto non valida/o"),
            @ApiResponse(code = 401, message = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(code = 404, message = "Feature non trovata per il TipoPosto specificato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/fromTipoPosto/{tipoPostoId}")
    public ResponseEntity<FeaturesDTO> getByTipoPostoId(@PathVariable Long tipoPostoId) {
        FeaturesDTO featureDTO = featuresService.getByTipoPostoId(tipoPostoId);
        return ResponseEntity.ok(featureDTO);
    }

    @ApiOperation(
            value = "Crea una nuova Features",
            notes = "Crea una nuova Features a partire da un DTO valido. Restituisce l'entità salvata.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Features creata"),
            @ApiResponse(code = 400, message = "Dati non validi"),
            @ApiResponse(code = 401, message = "Non autorizzato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @PostMapping("/save")  //IMPORTANTE se vogliamo rendere l'ui piu fluida dobbiamo "inglobarlo" nel create i tipo posto quindi alla creazione della "fascia" si crea la features anche perche il modulo da compilare credo saranno insieme
    public ResponseEntity<FeaturesDTO> create(@Valid @RequestBody FeaturesDTO dto) {
        FeaturesDTO created = featuresService.create(dto);
        return ResponseEntity.ok(created);
    }

    @ApiOperation(
            value = "Aggiorna una Features esistente",
            notes = "Aggiorna una Features utilizzando l'ID e i nuovi dati forniti nel DTO.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Features aggiornata"),
            @ApiResponse(code = 404, message = "Features non trovata con ID specificato"),
            @ApiResponse(code = 400, message = "Dati non validi"),
            @ApiResponse(code = 401, message = "Non autorizzato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @PutMapping("/{id}/update") //decidere
    public ResponseEntity<FeaturesDTO> update(@PathVariable Long id, @Valid @RequestBody FeaturesDTO dto) {
        FeaturesDTO updated = featuresService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

}
