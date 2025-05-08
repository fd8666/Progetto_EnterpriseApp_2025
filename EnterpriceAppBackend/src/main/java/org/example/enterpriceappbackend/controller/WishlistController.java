package org.example.enterpriceappbackend.controller;

import io.swagger.annotations.*;
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
@Api(value = "Wishlist API", description = "Operazioni relative alla gestione delle wishlist", tags = {"Wishlist"})
public class WishlistController {

    private final WishlistService wishlistService;

    @ApiOperation(value = "Recupera una Wishlist tramite ID", notes = "Restituisce una Wishlist specifica dato il suo ID.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Wishlist trovata"),
            @ApiResponse(code = 404, message = "Wishlist non trovata")
    })
    @GetMapping("/search/{id}")
    public ResponseEntity<WishlistDTO> getById(@PathVariable Long id) {
        return wishlistService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiOperation(value = "Recupera tutte le Wishlist", notes = "Restituisce tutte le wishlist presenti nel sistema.")
    @ApiResponse(code = 200, message = "Elenco wishlist restituito con successo")
    @GetMapping("")
    public List<WishlistDTO> getAll() {
        return wishlistService.findByAll();
    }

    @ApiOperation(value = "Recupera Wishlist di un utente", notes = "Restituisce tutte le wishlist associate a uno specifico utente.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Wishlist trovate"),
            @ApiResponse(code = 404, message = "Nessuna wishlist trovata per l'utente")
    })
    @GetMapping("/utente/{utenteId}")
    public List<WishlistDTO> getByUtente(@PathVariable Long utenteId) {
        return wishlistService.findByUtente(utenteId);
    }

    @ApiOperation(value = "Recupera Wishlist di un utente per visibilità", notes = "Restituisce le wishlist filtrate per utente e livello di visibilità.")
    @ApiResponse(code = 200, message = "Wishlist filtrate restituite con successo")
    @GetMapping("/utente/{utenteId}/visibilita/{visibilita}")
    public List<WishlistDTO> getByUtenteAndVisibilita(
            @PathVariable Long utenteId,
            @PathVariable Visibilita visibilita) {
        return wishlistService.findByUtenteAndVisibilita(utenteId, visibilita);
    }

    @ApiOperation(value = "Crea una nuova Wishlist", notes = "Crea e restituisce una nuova wishlist.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Wishlist creata"),
            @ApiResponse(code = 400, message = "Dati non validi")
    })
    @PostMapping("/create")
    public ResponseEntity<WishlistDTO> create(@RequestBody WishlistDTO wishlistDTO) {
        WishlistDTO created = wishlistService.create(wishlistDTO);
        return ResponseEntity.ok(created);
    }

    @ApiOperation(value = "Aggiorna una Wishlist esistente", notes = "Aggiorna una wishlist in base all'ID e ai dati forniti.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Wishlist aggiornata con successo"),
            @ApiResponse(code = 404, message = "Wishlist non trovata")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<WishlistDTO> update(@PathVariable Long id, @RequestBody WishlistDTO wishlistDTO) {
        WishlistDTO updated = wishlistService.update(id, wishlistDTO);
        return ResponseEntity.ok(updated);
    }

    @ApiOperation(value = "Elimina una Wishlist tramite ID", notes = "Elimina una wishlist specificata tramite ID.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Wishlist eliminata"),
            @ApiResponse(code = 404, message = "Wishlist non trovata")
    })
    @DeleteMapping("/evento/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        wishlistService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
