package org.example.enterpriceappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.dto.*;
import org.example.enterpriceappbackend.data.service.BigliettoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biglietto")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class BigliettoController {

    private final BigliettoService bigliettoService;


    @GetMapping("/{id}")
    public ResponseEntity<BigliettoInfoDTO> getBigliettoById(@PathVariable Long id) {
        return ResponseEntity.ok(bigliettoService.findById(id));
    }

    @GetMapping("/tipo-posto/{tipoPostoId}")
    public ResponseEntity<List<BigliettoInfoDTO>> getBigliettiByTipoPosto(@PathVariable Long tipoPostoId) {
        return ResponseEntity.ok(bigliettoService.findByTipoPosto(tipoPostoId));
    }


    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<BigliettoInfoDTO>> getBigliettiByEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(bigliettoService.findByEvento(eventoId));
    }


    @GetMapping("/{id}/prezzo")
    public ResponseEntity<Double> getPrezzoBiglietto(@PathVariable Long id) {
        return ResponseEntity.ok(bigliettoService.getPrezzoBiglietto(id));
    }


    @PostMapping("/create")
    public ResponseEntity<BigliettoInfoDTO> createBiglietto(@RequestBody BigliettoCreateDTO bigliettoCreateDTO) {
        return ResponseEntity.ok(bigliettoService.createBiglietto(bigliettoCreateDTO));
    }

    @GetMapping("/utente/{utenteId}")
    public ResponseEntity<List<BigliettoInfoDTO>> getBigliettiByUtente(@PathVariable Long utenteId) {
        return ResponseEntity.ok(bigliettoService.findByUtente(utenteId));
    }

    @PutMapping("/{id}/updateSpettatore")
    public ResponseEntity<BigliettoInfoDTO> updateSpettatore(
            @PathVariable Long id,
            @RequestBody BigliettoEditSpettatoreDTO bigliettoEditSpettatoreDTO) {
        return ResponseEntity.ok(bigliettoService.updateSpettatore(id, bigliettoEditSpettatoreDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBiglietto(@PathVariable Long id) {
        bigliettoService.deleteBiglietto(id);
        return ResponseEntity.noContent().build();
    }
}
