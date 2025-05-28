package it.unical.ea.eventra.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import it.unical.ea.eventra.data.service.OrdineService;
import it.unical.ea.eventra.dto.OrdineDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/ordine")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@Tag(name = "Ordine", description = "Gestione degli ordini (aggiunta, aggiornamento e cancellazione)")
public class OrdineController {

    private final OrdineService ordineService;

    @Operation(
            summary = "Aggiungi un nuovo ordine",
            description = "Crea un nuovo ordine associato a un determinato proprietario tramite il suo ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ordine creato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping("/aggiungi/{idProprietario}")
    public ResponseEntity<OrdineDTO> aggiungi(
            @RequestBody @Valid OrdineDTO ordinedto,
            @PathVariable Long idProprietario) {
        OrdineDTO ordine = ordineService.aggiungiOrdine(ordinedto, idProprietario);
        return ResponseEntity.status(HttpStatus.CREATED).body(ordine);
    }

    @Operation(
            summary = "Aggiorna un ordine esistente",
            description = "Aggiorna i dettagli di un ordine esistente tramite il suo ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordine aggiornato con successo"),
            @ApiResponse(responseCode = "404", description = "Ordine non trovato"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    })
    @PutMapping("/aggiorna/{ordineId}")
    public ResponseEntity<String> aggiornaOrdineProdotti(
            @PathVariable Long ordineId,
            @RequestBody OrdineDTO ordineDTO) {
        ordineService.update(ordineId, ordineDTO);
        return ResponseEntity.ok("ORDINE AGGIORNATO CON SUCCESSO");
    }

    @Operation(
            summary = "Elimina un ordine",
            description = "Elimina un ordine specifico tramite il suo ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordine eliminato con successo"),
            @ApiResponse(responseCode = "404", description = "Ordine non trovato")
    })
    @DeleteMapping("/elimina/{ordineId}")
    public ResponseEntity<String> eliminaOrdine(@PathVariable Long ordineId) {
        ordineService.delete(ordineId);
        return ResponseEntity.ok("ORDINE ELIMINATO CON SUCCESSO");
    }
}
