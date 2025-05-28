package it.unical.ea.eventra.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import it.unical.ea.eventra.dto.EventoDTO;
import it.unical.ea.eventra.data.service.EventoService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/evento")
@RequiredArgsConstructor
@Tag(name = "Evento", description = "Operazioni relative alla gestione degli eventi")
public class EventoController {

    private final EventoService eventoService;

    @Operation(
            summary = "Crea un nuovo evento",
            description = "Crea un nuovo evento nel sistema e restituisce i dettagli dell'evento creato."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evento creato con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping("/create")
    public ResponseEntity<EventoDTO> createEvento(@RequestBody EventoDTO eventoDTO) {
        EventoDTO createdEvento = eventoService.create(eventoDTO);
        return new ResponseEntity<>(createdEvento, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Ottieni evento per ID",
            description = "Restituisce i dettagli di un evento dato il suo ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento trovato"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EventoDTO> getEventoById(@PathVariable Long id) {
        EventoDTO evento = eventoService.findById(id);
        return ResponseEntity.ok(evento);
    }

    @Operation(
            summary = "Cerca eventi per luogo",
            description = "Restituisce una lista di eventi contenenti il testo indicato nel campo 'luogo' (ricerca parziale, case-insensitive)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eventi trovati"),
            @ApiResponse(responseCode = "404", description = "Nessun evento trovato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/luogo")
    public ResponseEntity<List<EventoDTO>> getEventiByLuogo(@RequestParam("luogo") String luogo) {
        List<EventoDTO> eventi = eventoService.findByLuogoContainingIgnoreCase(luogo);
        return ResponseEntity.ok(eventi);
    }

    @Operation(
            summary = "Filtra eventi per data",
            description = "Filtra gli eventi in base a un intervallo di date opzionale."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eventi filtrati correttamente"),
            @ApiResponse(responseCode = "400", description = "Formato data non valido"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/filtra")
    public ResponseEntity<List<EventoDTO>> filtraEventiPerData(
            @RequestParam(required = false) String dataInizio,
            @RequestParam(required = false) String dataFine) {
        try {
            LocalDateTime startDateTime = dataInizio != null ? LocalDate.parse(dataInizio).atStartOfDay() : null;
            LocalDateTime endDateTime = dataFine != null ? LocalDate.parse(dataFine).atTime(23, 59, 59) : null;

            List<EventoDTO> eventi = eventoService.findByDataOraAperturaCancelliBetween(startDateTime, endDateTime);
            return ResponseEntity.ok(eventi);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Ottieni tutti gli eventi",
            description = "Restituisce la lista completa degli eventi disponibili."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eventi recuperati"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("")
    public ResponseEntity<List<EventoDTO>> getAllEventi() {
        return ResponseEntity.ok(eventoService.findAll());
    }

    @Operation(
            summary = "Ottieni eventi per organizzatore",
            description = "Restituisce tutti gli eventi associati a uno specifico organizzatore."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eventi trovati"),
            @ApiResponse(responseCode = "404", description = "Nessun evento trovato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/organizzatore/{organizzatoreId}")
    public ResponseEntity<List<EventoDTO>> getEventiByOrganizzatore(@PathVariable Long organizzatoreId) {
        return ResponseEntity.ok(eventoService.findByOrganizzatore(organizzatoreId));
    }

    @Operation(
            summary = "Ottieni eventi per categoria",
            description = "Restituisce tutti gli eventi appartenenti a una categoria specifica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eventi trovati"),
            @ApiResponse(responseCode = "404", description = "Nessun evento trovato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<EventoDTO>> getEventiByCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(eventoService.findByCategoria(categoriaId));
    }

    @Operation(
            summary = "Ottieni eventi dopo una data",
            description = "Recupera eventi programmati dopo la data specificata."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eventi trovati"),
            @ApiResponse(responseCode = "400", description = "Data non valida"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/data-after")
    public ResponseEntity<List<EventoDTO>> getEventiAfterData(@RequestParam("data") String data) {
        LocalDate date = LocalDate.parse(data, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime dateTime = date.atStartOfDay();
        return ResponseEntity.ok(eventoService.findByData(dateTime));
    }

    @Operation(
            summary = "Cerca eventi per nome",
            description = "Esegue una ricerca parziale per nome degli eventi."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eventi trovati"),
            @ApiResponse(responseCode = "404", description = "Nessun evento trovato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/search")
    public ResponseEntity<List<EventoDTO>> getEventiByNome(@RequestParam("nome") String nome) {
        return ResponseEntity.ok(eventoService.findByNomeContaining(nome));
    }

    @Operation(
            summary = "Aggiorna un evento",
            description = "Aggiorna un evento esistente utilizzando il suo ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento aggiornato correttamente"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<EventoDTO> updateEvento(@PathVariable Long id, @RequestBody EventoDTO eventoDTO) {
        EventoDTO updatedEvento = eventoService.update(id, eventoDTO);
        return ResponseEntity.ok(updatedEvento);
    }

    @Operation(
            summary = "Elimina un evento",
            description = "Elimina un evento specificato dall'ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Evento eliminato con successo"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteEvento(@PathVariable Long id) {
        eventoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Scarica evento in formato ICS",
            description = "Genera un file ICS per esportare l'evento nel calendario dell'utente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File ICS generato con successo"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/{id}/ics")
    public ResponseEntity<byte[]> scaricaICS(@PathVariable Long id) {
        byte[] ics = eventoService.generaICS(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("evento.ics")
                .build());

        return new ResponseEntity<>(ics, headers, HttpStatus.OK);
    }
}
