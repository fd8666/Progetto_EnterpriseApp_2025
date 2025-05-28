package it.unical.ea.eventra.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unical.ea.eventra.dto.BigliettoCreateDTO;
import it.unical.ea.eventra.dto.BigliettoEditSpettatoreDTO;
import it.unical.ea.eventra.dto.BigliettoInfoDTO;
import lombok.RequiredArgsConstructor;
import it.unical.ea.eventra.data.service.BigliettoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biglietto")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@Tag(name = "Biglietto", description = "Operazioni relative alla gestione dei biglietti")
public class BigliettoController {

    private final BigliettoService bigliettoService;

    @Operation(
            summary = "Recupera biglietto per ID",
            description = "Restituisce un oggetto `BigliettoInfoDTO` per l'ID specificato."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Biglietto trovato"),
            @ApiResponse(responseCode = "404", description = "Biglietto non trovato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BigliettoInfoDTO> getBigliettoById(@PathVariable Long id) {
        return ResponseEntity.ok(bigliettoService.findById(id));
    }

    @Operation(
            summary = "Recupera biglietti per tipo posto",
            description = "Restituisce una lista di biglietti associati al tipo di posto specificato."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Biglietti trovati"),
            @ApiResponse(responseCode = "404", description = "Nessun biglietto trovato per il tipo di posto specificato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/tipo-posto/{tipoPostoId}")
    public ResponseEntity<List<BigliettoInfoDTO>> getBigliettiByTipoPosto(@PathVariable Long tipoPostoId) {
        return ResponseEntity.ok(bigliettoService.findByTipoPosto(tipoPostoId));
    }

    @Operation(
            summary = "Recupera biglietti per evento",
            description = "Restituisce una lista di biglietti associati all'ID dell'evento."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Biglietti trovati"),
            @ApiResponse(responseCode = "404", description = "Nessun biglietto trovato per l'evento specificato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<BigliettoInfoDTO>> getBigliettiByEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(bigliettoService.findByEvento(eventoId));
    }

    @Operation(
            summary = "Recupera prezzo biglietto",
            description = "Restituisce il prezzo del biglietto associato all'ID fornito."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prezzo recuperato con successo"),
            @ApiResponse(responseCode = "404", description = "Biglietto non trovato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/{id}/prezzo")
    public ResponseEntity<Double> getPrezzoBiglietto(@PathVariable Long id) {
        return ResponseEntity.ok(bigliettoService.getPrezzoBiglietto(id));
    }

    @Operation(
            summary = "Crea un nuovo biglietto",
            description = "Crea un nuovo biglietto a partire dai dati forniti nel `BigliettoCreateDTO`."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Biglietto creato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi per la creazione del biglietto"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping("/create")
    public ResponseEntity<BigliettoInfoDTO> createBiglietto(@RequestBody BigliettoCreateDTO bigliettoCreateDTO) {
        return ResponseEntity.ok(bigliettoService.createBiglietto(bigliettoCreateDTO));
    }

    @Operation(
            summary = "Recupera biglietti per utente",
            description = "Restituisce tutti i biglietti acquistati da un utente specifico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Biglietti trovati"),
            @ApiResponse(responseCode = "404", description = "Nessun biglietto trovato per l'utente"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/utente/{utenteId}")
    public ResponseEntity<List<BigliettoInfoDTO>> getBigliettiByUtente(@PathVariable Long utenteId) {
        return ResponseEntity.ok(bigliettoService.findByUtente(utenteId));
    }

    @Operation(
            summary = "Aggiorna spettatore del biglietto",
            description = "Modifica i dati dello spettatore per un biglietto esistente utilizzando `BigliettoEditSpettatoreDTO`."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Spettatore aggiornato con successo"),
            @ApiResponse(responseCode = "404", description = "Biglietto non trovato"),
            @ApiResponse(responseCode = "400", description = "Dati forniti non validi"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PutMapping("/{id}/updateSpettatore")
    public ResponseEntity<BigliettoInfoDTO> updateSpettatore(
            @PathVariable Long id,
            @RequestBody BigliettoEditSpettatoreDTO bigliettoEditSpettatoreDTO) {
        return ResponseEntity.ok(bigliettoService.updateSpettatore(id, bigliettoEditSpettatoreDTO));
    }

    @Operation(
            summary = "Elimina biglietto",
            description = "Elimina un biglietto esistente tramite ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Biglietto eliminato con successo"),
            @ApiResponse(responseCode = "404", description = "Biglietto non trovato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBiglietto(@PathVariable Long id) {
        bigliettoService.deleteBiglietto(id);
        return ResponseEntity.noContent().build();
    }
}
