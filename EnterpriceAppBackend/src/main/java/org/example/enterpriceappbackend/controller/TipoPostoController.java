package org.example.enterpriceappbackend.controller;

import io.swagger.annotations.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.service.TipoPostoService;
import org.example.enterpriceappbackend.dto.TipoPostoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipi-posto")
@RequiredArgsConstructor
@Api(value = "TipoPosto API", description = "Operazioni relative alla gestione dei tipi di posto", tags = {"TipoPosto"})
public class TipoPostoController {

    private final TipoPostoService tipoPostoService;

    @ApiOperation(
            value = "Crea un nuovo tipo di posto",
            notes = "Crea un oggetto `TipoPostoDTO` per un evento specifico. Richiede tutti i campi validati tramite `@Valid`."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Tipo di posto creato con successo"),
            @ApiResponse(code = 400, message = "Dati non validi forniti nel corpo della richiesta"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @PostMapping("/create")
    public ResponseEntity<TipoPostoDTO> create(@Valid @RequestBody TipoPostoDTO dto) {
        TipoPostoDTO created = tipoPostoService.createTipoPosto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @ApiOperation(
            value = "Ottieni tipo di posto per ID",
            notes = "Restituisce i dettagli del tipo di posto identificato tramite l'ID."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Tipo di posto trovato"),
            @ApiResponse(code = 404, message = "Tipo di posto non trovato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TipoPostoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tipoPostoService.getTipoPostoById(id));
    }

    @ApiOperation(
            value = "Ottieni tipi di posto per evento",
            notes = "Restituisce una lista di `TipoPostoDTO` associati a uno specifico evento."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Tipi di posto trovati"),
            @ApiResponse(code = 404, message = "Nessun tipo di posto trovato per l'evento specificato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<TipoPostoDTO>> getByEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(tipoPostoService.getTipiPostoByEvento(eventoId));
    }

    @ApiOperation(
            value = "Ottieni numero totale di posti per evento",
            notes = "Restituisce il totale dei posti disponibili per un evento specificato."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Totale posti calcolato correttamente"),
            @ApiResponse(code = 404, message = "Evento non trovato o nessun tipo di posto associato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/total-posti/{eventoId}")
    public ResponseEntity<Integer> getTotalPosti(@PathVariable Long eventoId) {
        return ResponseEntity.ok(tipoPostoService.getTotalPostiByEvento(eventoId));
    }

    @ApiOperation(
            value = "Ottieni tipo di posto per prezzo",
            notes = "Restituisce un `TipoPostoDTO` che corrisponde esattamente al prezzo indicato."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Tipo di posto trovato"),
            @ApiResponse(code = 404, message = "Nessun tipo di posto trovato per il prezzo specificato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/by-prezzo/{prezzo}")
    public ResponseEntity<TipoPostoDTO> getByPrezzo(@PathVariable Double prezzo) {
        return ResponseEntity.ok(tipoPostoService.getTipoPostoByPrezzo(prezzo));
    }
}
