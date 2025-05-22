package org.example.enterpriceappbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.constants.Visibilita;
import org.example.enterpriceappbackend.data.service.WishlistService;
import org.example.enterpriceappbackend.dto.WishlistDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Operazioni relative alla gestione delle wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "Ottieni wishlist per ID",
            description = "Recupera una wishlist specifica tramite il suo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist trovata"),
            @ApiResponse(responseCode = "404", description = "Wishlist non trovata con l'ID specificato"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/search/{id}")
    public ResponseEntity<WishlistDTO> getById(@PathVariable Long id) {
        return wishlistService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Ottieni tutte le wishlist",
            description = "Restituisce la lista completa di tutte le wishlist presenti nel sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista wishlist restituita con successo"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("")
    public List<WishlistDTO> getAll() {
        return wishlistService.findByAll();
    }

    @Operation(summary = "Ottieni wishlist per utente",
            description = "Recupera tutte le wishlist associate a uno specifico utente tramite ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist utente trovate"),
            @ApiResponse(responseCode = "404", description = "Nessuna wishlist trovata per l'utente"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/utente/{utenteId}")
    public List<WishlistDTO> getByUtente(@PathVariable Long utenteId) {
        return wishlistService.findByUtente(utenteId);
    }

    @Operation(summary = "Ottieni wishlist per utente e visibilità",
            description = "Recupera tutte le wishlist di un utente filtrate per visibilità.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist trovate"),
            @ApiResponse(responseCode = "404", description = "Nessuna wishlist trovata con i criteri specificati"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/utente/{utenteId}/visibilita/{visibilita}")
    public List<WishlistDTO> getByUtenteAndVisibilita(
            @PathVariable Long utenteId,
            @PathVariable Visibilita visibilita) {
        return wishlistService.findByUtenteAndVisibilita(utenteId, visibilita);
    }

    @Operation(summary = "Crea una nuova wishlist",
            description = "Crea una nuova wishlist basata sui dati forniti nel body della richiesta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist creata con successo"),
            @ApiResponse(responseCode = "400", description = "Dati della wishlist non validi"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping("/create")
    public ResponseEntity<WishlistDTO> create(@RequestBody WishlistDTO wishlistDTO) {
        WishlistDTO created = wishlistService.create(wishlistDTO);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Aggiorna una wishlist esistente",
            description = "Aggiorna i dati di una wishlist identificata dall'ID specificato.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist aggiornata con successo"),
            @ApiResponse(responseCode = "400", description = "Dati della wishlist non validi"),
            @ApiResponse(responseCode = "404", description = "Wishlist non trovata con l'ID specificato"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<WishlistDTO> update(@PathVariable Long id, @RequestBody WishlistDTO wishlistDTO) {
        WishlistDTO updated = wishlistService.update(id, wishlistDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Elimina una wishlist per ID",
            description = "Elimina la wishlist identificata dall'ID specificato.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Wishlist eliminata con successo"),
            @ApiResponse(responseCode = "404", description = "Wishlist non trovata con l'ID specificato"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @DeleteMapping("/evento/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        wishlistService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Rimuovi un evento da una wishlist",
            description = "Rimuove un evento specifico da una wishlist specificata da ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento rimosso dalla wishlist"),
            @ApiResponse(responseCode = "404", description = "Wishlist o evento non trovati"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @DeleteMapping("/{wishlistId}/evento/{eventoId}")
    public ResponseEntity<Void> removeEventoFromWishlist(
            @PathVariable Long wishlistId,
            @PathVariable Long eventoId) {
        wishlistService.removeEventoFromWishlist(wishlistId, eventoId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Aggiungi un evento a una wishlist",
            description = "Aggiunge un evento specifico a una wishlist identificata dall'ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento aggiunto alla wishlist"),
            @ApiResponse(responseCode = "404", description = "Wishlist o evento non trovati"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping("/{wishlistId}/evento/{eventoId}")
    public ResponseEntity<Void> addEventoToWishlist(
            @PathVariable Long wishlistId,
            @PathVariable Long eventoId) {
        wishlistService.addEventoToWishlist(wishlistId, eventoId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Ottieni wishlist condivise con un utente",
            description = "Recupera tutte le wishlist che sono condivise con l'utente specificato tramite ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist condivise trovate"),
            @ApiResponse(responseCode = "404", description = "Nessuna wishlist condivisa trovata"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/condiviseCon/{utenteId}")
    public List<WishlistDTO> getWishlistCondiviseConUtente(@PathVariable Long utenteId) {
        return wishlistService.findCondiviseConUtente(utenteId);
    }
}
