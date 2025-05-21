package org.example.enterpriceappbackend.controller;

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
public class WishlistCondivisaController {

    private final WishlistCondivisaService wishlistCondivisaService;


    @PostMapping("/create")
    public ResponseEntity<?> create( @RequestBody WishlistCondivisaDTO wishlistCondivisaDTO) {
        try {
            WishlistCondivisaDTO created = wishlistCondivisaService.create(wishlistCondivisaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("/remove/{wishlistId}/{utenteId}")
    public ResponseEntity<Void> rimuoviCondivisione(@PathVariable Long wishlistId, @PathVariable Long utenteId) {
        wishlistCondivisaService.rimuoviCondivisione(wishlistId, utenteId);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/remove-by-wishlist/{wishlistId}")
    public ResponseEntity<Void> rimuoviTutteCondivisioni(@PathVariable Long wishlistId) {
        wishlistCondivisaService.rimuoviTutteCondivisioni(wishlistId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-wishlist/{wishlistId}")
    public ResponseEntity<List<WishlistCondivisaDTO>> getByWishlistId(@PathVariable Long wishlistId) {
        List<WishlistCondivisaDTO> condivisioni = wishlistCondivisaService.findByWishlistId(wishlistId);
        return ResponseEntity.ok(condivisioni);
    }
}
