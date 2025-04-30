package org.example.enterpriceappbackend.data.controller;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.service.OrdineService;
import org.example.enterpriceappbackend.dto.OrdineDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("ordineController-api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor

public class OrdineController {

    private final OrdineService ordineService;

    @PostMapping("/aggiungi")
    public ResponseEntity<Long> aggiungi(@RequestBody OrdineDTO ordinedto){
        try{
            Long ordineId = ordineService.save(ordinedto);
            return ResponseEntity.status(HttpStatus.CREATED).body(ordineId);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/aggiorna/{ordineId}")
    public ResponseEntity<String> aggiornaOrdineProdotti(
            @PathVariable Long ordineId,
            @RequestBody List<OrdineDTO> ordinedto){
        try{
            ordineService.updateOrdineProdotti(ordineId, ordinedto);
            return ResponseEntity.ok("ORDINE AGGIORNATO CON SUCCESSO");
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERRORE NEL SERVER! ");
        }
    }

    

}
