package org.example.enterpriceappbackend.controller;

import io.swagger.annotations.*;
import org.example.enterpriceappbackend.dto.*;
import org.example.enterpriceappbackend.data.service.BigliettoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biglietto")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Api(value = "Biglietto API", description = "Operazioni relative alla gestione dei biglietti", tags = {"Biglietto"})
public class BigliettoController {

    private final BigliettoService bigliettoService;

    public BigliettoController(BigliettoService bigliettoService) {
        this.bigliettoService = bigliettoService;
    }

    @ApiOperation(value = "Recupera un biglietto tramite ID", notes = "Restituisce un oggetto BigliettoInfoDTO contenente le informazioni del biglietto.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Biglietto trovato"),
            @ApiResponse(code = 404, message = "Biglietto non trovato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BigliettoInfoDTO> getBigliettoById(@PathVariable Long id) {
        return ResponseEntity.ok(bigliettoService.findById(id));
    }

    @ApiOperation(value = "Recupera biglietti per tipo posto", notes = "Restituisce una lista di biglietti associati a un tipo posto specifico.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Lista di biglietti trovata"),
            @ApiResponse(code = 404, message = "Nessun biglietto trovato per il tipo posto specificato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/tipo-posto/{tipoPostoId}")
    public ResponseEntity<List<BigliettoInfoDTO>> getBigliettiByTipoPosto(@PathVariable Long tipoPostoId) {
        return ResponseEntity.ok(bigliettoService.findByTipoPosto(tipoPostoId));
    }

    @ApiOperation(value = "Recupera biglietti per evento", notes = "Restituisce una lista di biglietti associati a un evento specifico.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Lista di biglietti trovata"),
            @ApiResponse(code = 404, message = "Nessun biglietto trovato per l'evento specificato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<BigliettoInfoDTO>> getBigliettiByEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(bigliettoService.findByEvento(eventoId));
    }

    @ApiOperation(value = "Ottieni il prezzo di un biglietto", notes = "Restituisce il prezzo corrente di un biglietto tramite il suo ID.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Prezzo restituito con successo"),
            @ApiResponse(code = 404, message = "Biglietto non trovato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/{id}/prezzo")
    public ResponseEntity<Double> getPrezzoBiglietto(@PathVariable Long id) {
        return ResponseEntity.ok(bigliettoService.getPrezzoBiglietto(id));
    }

    @ApiOperation(value = "Crea un nuovo biglietto", notes = "Permette la creazione di un nuovo biglietto tramite il DTO BigliettoCreateDTO.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Biglietto creato con successo"),
            @ApiResponse(code = 400, message = "Dati non validi per la creazione"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @PostMapping("/create")
    public ResponseEntity<BigliettoInfoDTO> createBiglietto(@RequestBody BigliettoCreateDTO bigliettoCreateDTO) {
        return ResponseEntity.ok(bigliettoService.createBiglietto(bigliettoCreateDTO));
    }

    @ApiOperation(value = "Recupera biglietti acquistati da un utente", notes = "Restituisce tutti i biglietti acquistati da un determinato utente.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Lista di biglietti restituita con successo"),
            @ApiResponse(code = 404, message = "Nessun biglietto trovato per l'utente specificato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/utente/{utenteId}")
    public ResponseEntity<List<BigliettoInfoDTO>> getBigliettiByUtente(@PathVariable Long utenteId) {
        return ResponseEntity.ok(bigliettoService.findByUtente(utenteId));
    }

    @ApiOperation(value = "Aggiorna le informazioni dello spettatore", notes = "Modifica i dati dello spettatore associato a un biglietto.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Spettatore aggiornato con successo"),
            @ApiResponse(code = 404, message = "Biglietto non trovato"),
            @ApiResponse(code = 400, message = "Dati non validi per l'aggiornamento"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @PutMapping("/{id}/spettatore")
    public ResponseEntity<BigliettoInfoDTO> updateSpettatore(
            @PathVariable Long id,
            @RequestBody BigliettoEditSpettatoreDTO bigliettoEditSpettatoreDTO) {
        return ResponseEntity.ok(bigliettoService.updateSpettatore(id, bigliettoEditSpettatoreDTO));
    }

    @ApiOperation(value = "Elimina un biglietto", notes = "Esegue una cancellazione logica (soft delete) del biglietto con ID specificato.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Biglietto eliminato con successo"),
            @ApiResponse(code = 404, message = "Biglietto non trovato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBiglietto(@PathVariable Long id) {
        bigliettoService.deleteBiglietto(id);
        return ResponseEntity.noContent().build();
    }
}
