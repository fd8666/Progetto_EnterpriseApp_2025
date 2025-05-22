package org.example.enterpriceappbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "TipoPosto", description = "API per la gestione dei tipi di posto legati a un evento")
public class TipoPostoController {

    private final TipoPostoService tipoPostoService;

    @Operation(
            summary = "Crea un nuovo tipo di posto",
            description = "Crea e salva un nuovo tipo di posto (ad esempio VIP, Standard, ecc.) associato a un evento. "
                    + "Il corpo della richiesta deve contenere un oggetto TipoPostoDTO valido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tipo di posto creato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati forniti non validi")
    })
    @PostMapping("/create")
    public ResponseEntity<TipoPostoDTO> create(@Valid @RequestBody TipoPostoDTO dto) {
        TipoPostoDTO created = tipoPostoService.createTipoPosto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            summary = "Recupera un tipo di posto tramite ID",
            description = "Restituisce i dettagli di un tipo di posto specifico dato il suo ID univoco."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo di posto trovato"),
            @ApiResponse(responseCode = "404", description = "Nessun tipo di posto trovato con l'ID fornito")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TipoPostoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tipoPostoService.getTipoPostoById(id));
    }

    @Operation(
            summary = "Ottieni i tipi di posto associati a un evento",
            description = "Recupera tutti i tipi di posto (es. platea, galleria, VIP) associati a un evento specifico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipi di posto recuperati con successo"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato o senza tipi di posto associati")
    })
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<TipoPostoDTO>> getByEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(tipoPostoService.getTipiPostoByEvento(eventoId));
    }

    @Operation(
            summary = "Calcola il numero totale di posti per un evento",
            description = "Restituisce la somma totale dei posti disponibili per tutti i tipi di posto legati a un evento specifico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Totale dei posti restituito con successo"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    @GetMapping("/total-posti/{eventoId}")
    public ResponseEntity<Integer> getTotalPosti(@PathVariable Long eventoId) {
        return ResponseEntity.ok(tipoPostoService.getTotalPostiByEvento(eventoId));
    }

    @Operation(
            summary = "Recupera un tipo di posto tramite prezzo",
            description = "Restituisce un tipo di posto corrispondente a un prezzo specifico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo di posto trovato per il prezzo indicato"),
            @ApiResponse(responseCode = "404", description = "Nessun tipo di posto corrisponde al prezzo indicato")
    })
    @GetMapping("/by-prezzo/{prezzo}")
    public ResponseEntity<TipoPostoDTO> getByPrezzo(@PathVariable Double prezzo) {
        return ResponseEntity.ok(tipoPostoService.getTipoPostoByPrezzo(prezzo));
    }
}
