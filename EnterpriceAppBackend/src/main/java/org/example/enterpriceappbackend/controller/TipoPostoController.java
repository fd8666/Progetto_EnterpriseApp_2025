package org.example.enterpriceappbackend.controller;

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
@RequiredArgsConstructor public class TipoPostoController {

    private final TipoPostoService tipoPostoService;

    @PostMapping("/create")
    public ResponseEntity<TipoPostoDTO> create(@Valid @RequestBody TipoPostoDTO dto) {
        TipoPostoDTO created = tipoPostoService.createTipoPosto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoPostoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tipoPostoService.getTipoPostoById(id));
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<TipoPostoDTO>> getByEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(tipoPostoService.getTipiPostoByEvento(eventoId));
    }


    @GetMapping("/total-posti/{eventoId}")
    public ResponseEntity<Integer> getTotalPosti(@PathVariable Long eventoId) {
        return ResponseEntity.ok(tipoPostoService.getTotalPostiByEvento(eventoId));
    }

    @GetMapping("/by-prezzo/{prezzo}")
    public ResponseEntity<TipoPostoDTO> getByPrezzo(@PathVariable Double prezzo) {
        return ResponseEntity.ok(tipoPostoService.getTipoPostoByPrezzo(prezzo));
    }
}
