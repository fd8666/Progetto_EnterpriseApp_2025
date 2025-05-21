package org.example.enterpriceappbackend.controller;

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
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/search/{id}")
    public ResponseEntity<WishlistDTO> getById(@PathVariable Long id) {
        return wishlistService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("")
    public List<WishlistDTO> getAll() {
        return wishlistService.findByAll();
    }

    @GetMapping("/utente/{utenteId}")
    public List<WishlistDTO> getByUtente(@PathVariable Long utenteId) {
        return wishlistService.findByUtente(utenteId);
    }

    @GetMapping("/utente/{utenteId}/visibilita/{visibilita}")
    public List<WishlistDTO> getByUtenteAndVisibilita(
            @PathVariable Long utenteId,
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

    @DeleteMapping("/{wishlistId}/evento/{eventoId}")
    public ResponseEntity<Void> removeEventoFromWishlist(
            @PathVariable Long wishlistId,
            @PathVariable Long eventoId) {
        wishlistService.removeEventoFromWishlist(wishlistId, eventoId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{wishlistId}/evento/{eventoId}")
    public ResponseEntity<Void> addEventoToWishlist(
            @PathVariable Long wishlistId,
            @PathVariable Long eventoId) {
        wishlistService.addEventoToWishlist(wishlistId, eventoId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/condiviseCon/{utenteId}")
    public List<WishlistDTO> getWishlistCondiviseConUtente(@PathVariable Long utenteId) {
        return wishlistService.findCondiviseConUtente(utenteId);
    }



}
