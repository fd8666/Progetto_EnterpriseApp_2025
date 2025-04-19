package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.dto.WishlistDTO;

import java.util.List;

public interface WishlistService {
    WishlistDTO findById(Long id);
    List<WishlistDTO> findByUtente(Long utenteId);
    List<WishlistDTO> findByUtenteAndVisibilita(Long utenteId, String visibilita);
    WishlistDTO save(WishlistDTO wishlistDTO);
    WishlistDTO update(Long id, WishlistDTO wishlistDTO);
    void delete(Long id);

}