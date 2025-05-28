package it.unical.ea.eventra.data.repository;

import it.unical.ea.eventra.data.entity.Utente;
import it.unical.ea.eventra.data.entity.Wishlist;
import it.unical.ea.eventra.data.entity.WishlistCondivisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface WishlistCondivisaRepository extends JpaRepository<WishlistCondivisa, Long>, JpaSpecificationExecutor<WishlistCondivisa> {
    List<WishlistCondivisa> findByWishlist(Wishlist wishlist);
    void deleteByWishlistAndCondivisaCon(Wishlist wishlist, Utente utente);
    void deleteByWishlist(Wishlist wishlist);
}