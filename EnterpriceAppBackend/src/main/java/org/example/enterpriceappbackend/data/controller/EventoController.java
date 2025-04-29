package org.example.enterpriceappbackend.data.controller;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.dto.EventoDTO;
import org.example.enterpriceappbackend.data.service.EventoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "*",allowedHeaders = "*")
@RequestMapping("/api/eventi")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    // Creazione di un nuovo evento
    @PostMapping("/create")
    public ResponseEntity<EventoDTO> createEvento(@RequestBody EventoDTO eventoDTO) {
        EventoDTO createdEvento = eventoService.create(eventoDTO);
        return new ResponseEntity<>(createdEvento, HttpStatus.CREATED);
    }

    // Recupero di un evento per ID
    @GetMapping("/{id}")
    public ResponseEntity<EventoDTO> getEventoById(@PathVariable Long id) {
        EventoDTO evento = eventoService.findById(id);
        return new ResponseEntity<>(evento, HttpStatus.OK);
    }
    //recupero evento tramite luogo
    @GetMapping("/luogo")
    public ResponseEntity<List<EventoDTO>> getEventiByLuogo(@RequestParam("luogo") String luogo) {
        List<EventoDTO> eventi = eventoService.findByLuogoContainingIgnoreCase(luogo);
        return new ResponseEntity<>(eventi, HttpStatus.OK);
    }
    //recupero evento tramite filtro data inizio e data fine
    @GetMapping("/filtra")
    public ResponseEntity<List<EventoDTO>> findByDataOraAperturaCancelliBetween(
            @RequestParam(required = false) String dataInizio,
            @RequestParam(required = false) String dataFine
    ) {
        // Conversione delle date da String a LocalDateTime
        LocalDateTime dataInizioParsed = dataInizio != null ? LocalDateTime.parse(dataInizio) : null;
        LocalDateTime dataFineParsed = dataFine != null ? LocalDateTime.parse(dataFine) : null;

        List<EventoDTO> eventi = eventoService.findByDataOraAperturaCancelliBetween(
                dataInizioParsed, dataFineParsed
        );
        return new ResponseEntity<>(eventi, HttpStatus.OK);
    }

    // Recupero di tutti gli eventi
    @GetMapping("")
    public ResponseEntity<List<EventoDTO>> getAllEventi() {
        List<EventoDTO> eventi = eventoService.findAll();
        return new ResponseEntity<>(eventi, HttpStatus.OK);
    }

    // Recupero di eventi per Organizzatore
    @GetMapping("/organizzatore/{organizzatoreId}")
    public ResponseEntity<List<EventoDTO>> getEventiByOrganizzatore(@PathVariable Long organizzatoreId) {
        List<EventoDTO> eventi = eventoService.findByOrganizzatore(organizzatoreId);
        return new ResponseEntity<>(eventi, HttpStatus.OK);
    }

    // Recupero di eventi per Categoria
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<EventoDTO>> getEventiByCategoria(@PathVariable Long categoriaId) {
        List<EventoDTO> eventi = eventoService.findByCategoria(categoriaId);
        return new ResponseEntity<>(eventi, HttpStatus.OK);
    }

    // Recupero di eventi dopo una certa data
    @GetMapping("/data-after")
    public ResponseEntity<List<EventoDTO>> getEventiAfterData(@RequestParam("data") String data) {
        List<EventoDTO> eventi = eventoService.findByData(LocalDateTime.parse(data));
        return new ResponseEntity<>(eventi, HttpStatus.OK);
    }

    // Recupero di eventi per nome
    @GetMapping("/search")
    public ResponseEntity<List<EventoDTO>> getEventiByNome(@RequestParam("nome") String nome) {
        List<EventoDTO> eventi = eventoService.findByNomeContaining(nome);
        return new ResponseEntity<>(eventi, HttpStatus.OK);
    }

    // Aggiornamento di un evento
    @PutMapping("/{id}")
    public ResponseEntity<EventoDTO> updateEvento(@PathVariable Long id, @RequestBody EventoDTO eventoDTO) {
        EventoDTO updatedEvento = eventoService.update(id, eventoDTO);
        return new ResponseEntity<>(updatedEvento, HttpStatus.OK);
    }

    // Eliminazione di un evento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvento(@PathVariable Long id) {
        eventoService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
