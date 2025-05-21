package org.example.enterpriceappbackend.controller;

import io.swagger.annotations.*;
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
@Api(value = "Ordine API", description = "Operazioni relative alla gestione degli ordini", tags = {"Ordini"})
public class OrdineController {

    private final OrdineService ordineService;

    // CORRETTO
    @ApiOperation(value = "Crea un nuovo ordine", response = Long.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Ordine creato con successo"),
            @ApiResponse(code = 400, message = "Dati non validi"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @PostMapping("/aggiungi/{idProprietario}")
    public ResponseEntity<OrdineDTO> aggiungi(
            @RequestBody @Valid OrdineDTO ordinedto,
            @PathVariable Long idProprietario) {
        OrdineDTO ordine = ordineService.aggiungiOrdine(ordinedto, idProprietario);
        return ResponseEntity.status(HttpStatus.CREATED).body(ordine);
    }


    @ApiOperation(value = "Aggiorna un ordine esistente")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ordine aggiornato con successo"),
            @ApiResponse(code = 400, message = "Dati non validi"),
            @ApiResponse(code = 404, message = "Ordine non trovato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @PutMapping("/aggiorna/{ordineId}")
    public ResponseEntity<String> aggiornaOrdineProdotti(
            @ApiParam(value = "ID dell'ordine da aggiornare", required = true)
            @PathVariable Long ordineId,
            @RequestBody OrdineDTO ordineDTO){

            ordineService.update(ordineId, ordineDTO);
            return ResponseEntity.ok("ORDINE AGGIORNATO CON SUCCESSO");
    }

    @ApiOperation(value = "Elimina un ordine esistente")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ordine eliminato con successo"),
            @ApiResponse(code = 400, message = "Dati non validi"),
            @ApiResponse(code = 404, message = "Ordine non trovato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @DeleteMapping("/elimina/{ordineId}")
    public ResponseEntity<String> eliminaOrdine(
            @ApiParam(value = "ID dell'ordine da eliminare", required = true)
            @PathVariable Long ordineId) {

            ordineService.delete(ordineId);
            return ResponseEntity.ok("ORDINE ELIMINATO CON SUCCESSO");

    }
}
