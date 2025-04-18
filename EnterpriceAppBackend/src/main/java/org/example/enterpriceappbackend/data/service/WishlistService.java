package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.dto.WishlistDto;

import java.util.List;

public interface WishlistService {
    WishlistDto findById(Long id);
    List<WishlistDto> findByProprietario(Long proprietarioId);
    List<WishlistDto> findByProprietarioAndVisibilita(Long proprietarioId, String visibilita);
    WishlistDto save(WishlistDto wishlistDTO);
    WishlistDto update(Long id, WishlistDto wishlistDTO);
    void delete(Long id);
    void addEventoToWishlist(Long wishlistId, Long eventoId);
    void removeEventoFromWishlist(Long wishlistId, Long eventoId);
}