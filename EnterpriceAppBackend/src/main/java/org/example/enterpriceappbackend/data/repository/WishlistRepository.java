package org.example.enterpriceappbackend.data.repository;

import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository //metodi gia implementati grazie a JpaRepository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> , JpaSpecificationExecutor<Wishlist> {
    List<Wishlist> findByUtente(Utente utente);
    List<Wishlist> findByUtenteAndVisibilita(Utente utente, String visibilita);

}
