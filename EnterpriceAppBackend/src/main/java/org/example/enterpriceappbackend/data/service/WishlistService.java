package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.dto.WishlistDto;

import java.util.List;

public interface WishlistService {
    WishlistDto findById(Long id);
    List<WishlistDto> findByUtente(Long utenteId);
    List<WishlistDto> findByUtenteAndVisibilita(Long utenteId, String visibilita);
    WishlistDto save(WishlistDto wishlistDTO);
    WishlistDto update(Long id, WishlistDto wishlistDTO);
    void delete(Long id);

}