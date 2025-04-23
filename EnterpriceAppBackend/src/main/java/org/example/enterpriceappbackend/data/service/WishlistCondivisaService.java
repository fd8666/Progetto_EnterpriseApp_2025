package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.dto.WishlistCondivisaDTO;

import java.util.List;

public interface WishlistCondivisaService {
    List<WishlistCondivisaDTO> findByWishlistId(Long id);
    void condividi(Long wishlistId);
    void rimuoviCondivisione(Long wishlistId, Long utenteId);

}
