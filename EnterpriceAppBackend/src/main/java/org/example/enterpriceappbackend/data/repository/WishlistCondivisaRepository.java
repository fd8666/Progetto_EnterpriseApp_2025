package org.example.enterpriceappbackend.data.repository;

import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.entity.Wishlist;
import org.example.enterpriceappbackend.data.entity.WishlistCondivisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface WishlistCondivisaRepository extends JpaRepository<WishlistCondivisa, Long> , JpaSpecificationExecutor<WishlistCondivisa> {
    List<WishlistCondivisa> findByWishlist(Wishlist wishlist);
    List<WishlistCondivisa> findByCondivisaCon(Utente utente);
    Boolean existsByWishlistAndCondivisaCon(Wishlist wishlist,Utente utente);
    void deleteByWishlistAndCondivisaCon(Wishlist wishlist, Utente utente);
}
