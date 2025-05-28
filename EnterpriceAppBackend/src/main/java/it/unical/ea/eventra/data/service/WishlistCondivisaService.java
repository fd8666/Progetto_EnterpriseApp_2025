package it.unical.ea.eventra.data.service;

import it.unical.ea.eventra.dto.WishlistCondivisaDTO;
import java.util.List;

public interface WishlistCondivisaService {
    List<WishlistCondivisaDTO> findByWishlistId(Long id);
    void condividi(Long wishlistId);
    void rimuoviCondivisione(Long wishlistId, Long utenteId);
    WishlistCondivisaDTO create(WishlistCondivisaDTO wishlistCondivisaDTO);
    void rimuoviTutteCondivisioni(Long wishlistId);
}