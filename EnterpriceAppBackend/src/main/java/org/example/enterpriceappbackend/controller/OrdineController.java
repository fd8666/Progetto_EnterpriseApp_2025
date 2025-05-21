package org.example.enterpriceappbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.service.OrdineService;
import org.example.enterpriceappbackend.dto.OrdineDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//FUNZIONANTE
@RestController
@RequestMapping("api/ordine")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class OrdineController {

    private final OrdineService ordineService;

    @PostMapping("/aggiungi/{idProprietario}")
    public ResponseEntity<OrdineDTO> aggiungi(
            @RequestBody @Valid OrdineDTO ordinedto,
            @PathVariable Long idProprietario) {
        OrdineDTO ordine = ordineService.aggiungiOrdine(ordinedto, idProprietario);
        return ResponseEntity.status(HttpStatus.CREATED).body(ordine);
    }

    @PutMapping("/aggiorna/{ordineId}")
    public ResponseEntity<String> aggiornaOrdineProdotti(@PathVariable Long ordineId,
            @RequestBody OrdineDTO ordineDTO){

            ordineService.update(ordineId, ordineDTO);
            return ResponseEntity.ok("ORDINE AGGIORNATO CON SUCCESSO");
    }


    @DeleteMapping("/elimina/{ordineId}")
    public ResponseEntity<String> eliminaOrdine(  @PathVariable Long ordineId) {

            ordineService.delete(ordineId);
            return ResponseEntity.ok("ORDINE ELIMINATO CON SUCCESSO");

    }
}
