package it.unical.ea.eventra.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import it.unical.ea.eventra.data.service.FeaturesService;
import it.unical.ea.eventra.dto.FeaturesDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/features")
@RequiredArgsConstructor
@Tag(name = "Features", description = "Gestione delle features associate ai tipi di posto")
public class FeaturesController {

    private final FeaturesService featuresService;

    @Operation(
            summary = "Recupera features da ID tipo posto",
            description = "Restituisce un oggetto `FeaturesDTO` contenente le informazioni delle features associate a uno specifico `TipoPosto`, identificato tramite ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Features trovate con successo"),
            @ApiResponse(responseCode = "404", description = "Nessuna feature trovata per il tipo posto specificato"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/fromTipoPosto/{tipoPostoId}")
    public ResponseEntity<FeaturesDTO> getByTipoPostoId(@PathVariable Long tipoPostoId) {
        FeaturesDTO featureDTO = featuresService.getByTipoPostoId(tipoPostoId);
        return ResponseEntity.ok(featureDTO);
    }

    @Operation(
            summary = "Crea nuove features",
            description = "Permette la creazione di un nuovo oggetto `FeaturesDTO`. Tipicamente utilizzato durante la creazione del `TipoPosto`."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Features create con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping("/save")
    public ResponseEntity<FeaturesDTO> create(@Valid @RequestBody FeaturesDTO dto) {
        FeaturesDTO created = featuresService.create(dto);
        return ResponseEntity.ok(created);
    }

    @Operation(
            summary = "Aggiorna features esistenti",
            description = "Aggiorna un oggetto `FeaturesDTO` esistente identificato tramite ID. I campi modificabili sono quelli previsti nel DTO."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Features aggiornate con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi per l'aggiornamento"),
            @ApiResponse(responseCode = "404", description = "Nessuna feature trovata con l'ID specificato"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PutMapping("/{id}/update")
    public ResponseEntity<FeaturesDTO> update(@PathVariable Long id, @Valid @RequestBody FeaturesDTO dto) {
        FeaturesDTO updated = featuresService.update(id, dto);
        return ResponseEntity.ok(updated);
    }
}
