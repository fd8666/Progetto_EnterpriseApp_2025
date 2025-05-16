package org.example.enterpriceappbackend.controller;

import io.swagger.annotations.*;
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
@Api(value = "Wishlist Condivisa API", tags = "Gestione Wishlist Condivise", description = "Operazioni per gestire la condivisione delle wishlist tra utenti")
public class WishlistCondivisaController {

    private final WishlistCondivisaService wishlistCondivisaService;

    @ApiOperation(
            value = "Crea una nuova condivisione di wishlist",
            notes = "Permette di condividere una wishlist con un altro utente creando un oggetto WishlistCondivisaDTO"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Condivisione creata con successo"),
            @ApiResponse(code = 400, message = "Dati non validi o errore di business"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @PostMapping("/create")
    public ResponseEntity<?> create(
            @ApiParam(value = "DTO della condivisione della wishlist", required = true)
            @RequestBody WishlistCondivisaDTO wishlistCondivisaDTO) {
        try {
            WishlistCondivisaDTO created = wishlistCondivisaService.create(wishlistCondivisaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @ApiOperation(
            value = "Rimuove una condivisione specifica",
            notes = "Elimina la condivisione di una wishlist per uno specifico utente"
    )
    @ApiResponses({
            @ApiResponse(code = 204, message = "Condivisione rimossa con successo"),
            @ApiResponse(code = 404, message = "Wishlist o utente non trovato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @DeleteMapping("/remove/{wishlistId}/{utenteId}")
    public ResponseEntity<Void> rimuoviCondivisione(
            @ApiParam(value = "ID della wishlist", required = true) @PathVariable Long wishlistId,
            @ApiParam(value = "ID dell'utente", required = true) @PathVariable Long utenteId) {
        wishlistCondivisaService.rimuoviCondivisione(wishlistId, utenteId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(
            value = "Rimuove tutte le condivisioni di una wishlist",
            notes = "Elimina tutte le condivisioni associate a una determinata wishlist"
    )
    @ApiResponses({
            @ApiResponse(code = 204, message = "Tutte le condivisioni rimosse con successo"),
            @ApiResponse(code = 404, message = "Wishlist non trovata"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @DeleteMapping("/remove-by-wishlist/{wishlistId}")
    public ResponseEntity<Void> rimuoviTutteCondivisioni(
            @ApiParam(value = "ID della wishlist", required = true) @PathVariable Long wishlistId) {
        wishlistCondivisaService.rimuoviTutteCondivisioni(wishlistId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(
            value = "Ottieni tutte le condivisioni di una wishlist",
            notes = "Recupera tutte le condivisioni legate a una wishlist specifica"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Condivisioni trovate e restituite"),
            @ApiResponse(code = 404, message = "Wishlist non trovata"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/by-wishlist/{wishlistId}")
    public ResponseEntity<List<WishlistCondivisaDTO>> getByWishlistId(
            @ApiParam(value = "ID della wishlist", required = true) @PathVariable Long wishlistId) {
        List<WishlistCondivisaDTO> condivisioni = wishlistCondivisaService.findByWishlistId(wishlistId);
        return ResponseEntity.ok(condivisioni);
    }
}
