package org.example.enterpriceappbackend.controller;


import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.dto.EventoDTO;
import org.example.enterpriceappbackend.data.service.EventoService;
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
public class EventoController {

    private final EventoService eventoService;

    @PostMapping("/create")
    public ResponseEntity<EventoDTO> createEvento(@RequestBody EventoDTO eventoDTO) {
        EventoDTO createdEvento = eventoService.create(eventoDTO);
        return new ResponseEntity<>(createdEvento, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoDTO> getEventoById(@PathVariable Long id) {
        EventoDTO evento = eventoService.findById(id);
        return ResponseEntity.ok(evento);
    }

    @GetMapping("/luogo")
    public ResponseEntity<List<EventoDTO>> getEventiByLuogo(@RequestParam("luogo") String luogo) {
        List<EventoDTO> eventi = eventoService.findByLuogoContainingIgnoreCase(luogo);
        return ResponseEntity.ok(eventi);
    }

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

    @GetMapping("")
    public ResponseEntity<List<EventoDTO>> getAllEventi() {
        return ResponseEntity.ok(eventoService.findAll());
    }

    @GetMapping("/organizzatore/{organizzatoreId}")
    public ResponseEntity<List<EventoDTO>> getEventiByOrganizzatore(@PathVariable Long organizzatoreId) {
        return ResponseEntity.ok(eventoService.findByOrganizzatore(organizzatoreId));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<EventoDTO>> getEventiByCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(eventoService.findByCategoria(categoriaId));
    }

    @GetMapping("/data-after")
    public ResponseEntity<List<EventoDTO>> getEventiAfterData(@RequestParam("data") String data) {
        // Usa il formato corretto che corrisponde a quello che ricevi dal frontend
        LocalDate date = LocalDate.parse(data, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime dateTime = date.atStartOfDay();
        return ResponseEntity.ok(eventoService.findByData(dateTime));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventoDTO>> getEventiByNome(@RequestParam("nome") String nome) {
        return ResponseEntity.ok(eventoService.findByNomeContaining(nome));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<EventoDTO> updateEvento(@PathVariable Long id, @RequestBody EventoDTO eventoDTO) {
        EventoDTO updatedEvento = eventoService.update(id, eventoDTO);
        return ResponseEntity.ok(updatedEvento);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteEvento(@PathVariable Long id) {
        eventoService.delete(id);
        return ResponseEntity.noContent().build();
    }

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
