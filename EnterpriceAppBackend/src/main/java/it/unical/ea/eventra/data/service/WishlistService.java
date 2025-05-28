package it.unical.ea.eventra.data.service;

import it.unical.ea.eventra.data.constants.Visibilita;
import it.unical.ea.eventra.dto.WishlistDTO;

import java.util.List;
import java.util.Optional;

public interface WishlistService {
    Optional<WishlistDTO> findById(Long id);
    List<WishlistDTO> findByAll();
    List<WishlistDTO> findByUtente(Long utenteId);


    void removeEventoFromWishlist(Long wishlistId, Long eventoId);

    List<WishlistDTO> findCondiviseConUtente(Long utenteId);

    void addEventoToWishlist(Long wishlistId, Long eventoId);
    List<WishlistDTO> findByUtenteAndVisibilita(Long utenteId, Visibilita visibilita);

    //CRUD
    WishlistDTO create(WishlistDTO wishlistDTO);
    WishlistDTO save(WishlistDTO wishlistDTO);
    WishlistDTO update(Long id, WishlistDTO wishlistDTO);
    void deleteById(Long id);


}