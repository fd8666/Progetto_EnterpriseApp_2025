package org.example.enterpriceappbackend.data.controller;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.constants.Visibilita;
import org.example.enterpriceappbackend.data.service.WishlistService;
import org.example.enterpriceappbackend.dto.WishlistDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    // Wishlist tramite id
    @GetMapping("/search/{id}")
    public ResponseEntity<WishlistDTO> getById(@PathVariable Long id) {
        return wishlistService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Prende tutte le wishlist
    @GetMapping("")
    public List<WishlistDTO> getAll() {
        return wishlistService.findByAll();
    }

    // Wishlist tramite utente id
    @GetMapping("/utente/{utenteId}")
    public List<WishlistDTO> getByUtente(@PathVariable Long utenteId) {
        return wishlistService.findByUtente(utenteId);
    }

    // Tramite utente id
    @GetMapping("/utente/{utenteId}/visibilita/{visibilita}")
    public List<WishlistDTO> getByUtenteAndVisibilita(@PathVariable Long utenteId,
                                                      @PathVariable Visibilita visibilita) {
        return wishlistService.findByUtenteAndVisibilita(utenteId, visibilita);
    }


    @PostMapping("/create")
    public ResponseEntity<WishlistDTO> create(@RequestBody WishlistDTO wishlistDTO) {
        WishlistDTO created = wishlistService.create(wishlistDTO);

        return ResponseEntity.ok(created);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<WishlistDTO> update(@PathVariable Long id, @RequestBody WishlistDTO wishlistDTO) {
        WishlistDTO updated = wishlistService.update(id, wishlistDTO);
        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/evento/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        wishlistService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
