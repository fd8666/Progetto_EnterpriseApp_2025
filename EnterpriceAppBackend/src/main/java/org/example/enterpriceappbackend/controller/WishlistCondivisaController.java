package org.example.enterpriceappbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.service.WishlistCondivisaService;
import org.example.enterpriceappbackend.dto.WishlistCondivisaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist/condivisa")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@Tag(name = "Wishlist Condivisa", description = "API per la gestione delle condivisioni delle wishlist")
public class WishlistCondivisaController {

    private final WishlistCondivisaService wishlistCondivisaService;

    @Operation(summary = "Crea una nuova condivisione di wishlist", description = "Crea una nuova condivisione associando una wishlist a un utente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Condivisione creata con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi o errore nella creazione")
    })
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody WishlistCondivisaDTO wishlistCondivisaDTO) {
        try {
            WishlistCondivisaDTO created = wishlistCondivisaService.create(wishlistCondivisaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Rimuove la condivisione di una wishlist con un utente", description = "Rimuove la condivisione specifica tra wishlist e utente.")
    @ApiResponse(responseCode = "204", description = "Condivisione rimossa con successo")
    @DeleteMapping("/remove/{wishlistId}/{utenteId}")
    public ResponseEntity<Void> rimuoviCondivisione(@PathVariable Long wishlistId, @PathVariable Long utenteId) {
        wishlistCondivisaService.rimuoviCondivisione(wishlistId, utenteId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Rimuove tutte le condivisioni di una wishlist", description = "Rimuove tutte le condivisioni associate a una wishlist specifica.")
    @ApiResponse(responseCode = "204", description = "Tutte le condivisioni rimosse con successo")
    @DeleteMapping("/remove-by-wishlist/{wishlistId}")
    public ResponseEntity<Void> rimuoviTutteCondivisioni(@PathVariable Long wishlistId) {
        wishlistCondivisaService.rimuoviTutteCondivisioni(wishlistId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Recupera tutte le condivisioni per una wishlist", description = "Restituisce la lista di tutte le condivisioni associate a una wishlist specifica.")
    @ApiResponse(responseCode = "200", description = "Lista condivisioni restituita con successo")
    @GetMapping("/by-wishlist/{wishlistId}")
    public ResponseEntity<List<WishlistCondivisaDTO>> getByWishlistId(@PathVariable Long wishlistId) {
        List<WishlistCondivisaDTO> condivisioni = wishlistCondivisaService.findByWishlistId(wishlistId);
        return ResponseEntity.ok(condivisioni);
    }
}
