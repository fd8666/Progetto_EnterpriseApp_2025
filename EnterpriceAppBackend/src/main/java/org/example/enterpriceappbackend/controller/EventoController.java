package org.example.enterpriceappbackend.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.dto.EventoDTO;
import org.example.enterpriceappbackend.data.service.EventoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/evento")
@RequiredArgsConstructor
@Api(value = "Evento API", description = "Operazioni relative alla gestione degli eventi", tags = {"Eventi"})
public class EventoController {

    private final EventoService eventoService;

    @ApiOperation(value = "Crea un nuovo Evento", notes = "Crea un evento a partire da un DTO valido.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Evento creato con successo"),
            @ApiResponse(code = 400, message = "Dati non validi"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @PostMapping("/create")
    public ResponseEntity<EventoDTO> createEvento(@RequestBody EventoDTO eventoDTO) {
        EventoDTO createdEvento = eventoService.create(eventoDTO);
        return new ResponseEntity<>(createdEvento, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Recupera un Evento per ID", notes = "Restituisce l'evento corrispondente all'ID fornito.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Evento trovato"),
            @ApiResponse(code = 404, message = "Evento non trovato")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EventoDTO> getEventoById(@PathVariable Long id) {
        EventoDTO evento = eventoService.findById(id);
        return ResponseEntity.ok(evento);
    }

    @ApiOperation(value = "Recupera eventi per luogo", notes = "Restituisce eventi che contengono il luogo indicato (case-insensitive).")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Eventi trovati")
    })
    @GetMapping("/luogo")
    public ResponseEntity<List<EventoDTO>> getEventiByLuogo(@RequestParam("luogo") String luogo) {
        List<EventoDTO> eventi = eventoService.findByLuogoContainingIgnoreCase(luogo);
        return ResponseEntity.ok(eventi);
    }

    @ApiOperation(value = "Filtra eventi per intervallo di date", notes = "Restituisce eventi le cui date di apertura cancelli sono comprese tra le date fornite.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Eventi filtrati trovati"),
            @ApiResponse(code = 400, message = "Formato data non valido")
    })
    @GetMapping("/filtra")
    public ResponseEntity<List<EventoDTO>> filtraEventiPerData(
            @RequestParam(required = false) String dataInizio,
            @RequestParam(required = false) String dataFine) {


        try {
            // Converti le stringhe in LocalDateTime
            LocalDateTime startDateTime = dataInizio != null ?
                    LocalDate.parse(dataInizio).atStartOfDay() : null;
            LocalDateTime endDateTime = dataFine != null ?
                    LocalDate.parse(dataFine).atTime(23, 59, 59) : null;

            List<EventoDTO> eventi = eventoService.findByDataOraAperturaCancelliBetween(
                    startDateTime,
                    endDateTime
            );


            return ResponseEntity.ok(eventi);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().build();
        }
    }

    @ApiOperation(value = "Recupera tutti gli eventi", notes = "Restituisce l'elenco completo degli eventi presenti.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Lista di eventi recuperata")
    })
    @GetMapping("")
    public ResponseEntity<List<EventoDTO>> getAllEventi() {
        return ResponseEntity.ok(eventoService.findAll());
    }

    @ApiOperation(value = "Recupera eventi per ID organizzatore", notes = "Restituisce eventi associati a un organizzatore specifico.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Eventi trovati")
    })
    @GetMapping("/organizzatore/{organizzatoreId}")
    public ResponseEntity<List<EventoDTO>> getEventiByOrganizzatore(@PathVariable Long organizzatoreId) {
        return ResponseEntity.ok(eventoService.findByOrganizzatore(organizzatoreId));
    }

    @ApiOperation(value = "Recupera eventi per ID categoria", notes = "Restituisce eventi appartenenti a una certa categoria.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Eventi trovati")
    })
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<EventoDTO>> getEventiByCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(eventoService.findByCategoria(categoriaId));
    }

    @ApiOperation(value = "Recupera eventi dopo una certa data", notes = "Restituisce eventi con apertura cancelli successiva alla data indicata.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Eventi trovati"),
            @ApiResponse(code = 400, message = "Formato data non valido")
    })
    @GetMapping("/data-after")
    public ResponseEntity<List<EventoDTO>> getEventiAfterData(@RequestParam("data") String data) {
        // Usa il formato corretto che corrisponde a quello che ricevi dal frontend
        LocalDate date = LocalDate.parse(data, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime dateTime = date.atStartOfDay();
        return ResponseEntity.ok(eventoService.findByData(dateTime));
    }

    @ApiOperation(value = "Ricerca eventi per nome", notes = "Restituisce eventi contenenti il nome indicato.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Eventi trovati")
    })
    @GetMapping("/search")
    public ResponseEntity<List<EventoDTO>> getEventiByNome(@RequestParam("nome") String nome) {
        return ResponseEntity.ok(eventoService.findByNomeContaining(nome));
    }

    @ApiOperation(value = "Aggiorna un Evento", notes = "Aggiorna un evento tramite ID e DTO forniti.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Evento aggiornato"),
            @ApiResponse(code = 404, message = "Evento non trovato"),
            @ApiResponse(code = 400, message = "Dati non validi")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<EventoDTO> updateEvento(@PathVariable Long id, @RequestBody EventoDTO eventoDTO) {
        EventoDTO updatedEvento = eventoService.update(id, eventoDTO);
        return ResponseEntity.ok(updatedEvento);
    }

    @ApiOperation(value = "Elimina un Evento", notes = "Elimina l'evento corrispondente all'ID fornito.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Evento eliminato con successo"),
            @ApiResponse(code = 404, message = "Evento non trovato")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteEvento(@PathVariable Long id) {
        eventoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
