package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.data.constants.Visibilita;
import org.example.enterpriceappbackend.dto.WishlistDTO;

import java.util.List;
import java.util.Optional;

public interface WishlistService {
    Optional<WishlistDTO> findById(Long id);
    List<WishlistDTO> findByAll();
    List<WishlistDTO> findByUtente(Long utenteId);
    List<WishlistDTO> findByUtenteAndVisibilita(Long utenteId, Visibilita visibilita);

    //CRUD
    WishlistDTO create(WishlistDTO wishlistDTO);
    WishlistDTO save(WishlistDTO wishlistDTO);
    WishlistDTO update(Long id, WishlistDTO wishlistDTO);
    void deleteById(Long id);

}