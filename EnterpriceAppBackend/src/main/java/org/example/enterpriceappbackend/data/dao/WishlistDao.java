package org.example.enterpriceappbackend.data.dao;

import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistDao extends JpaRepository<Wishlist, Long> , JpaSpecificationExecutor<Wishlist> {
    List<Wishlist> findByProprietario(Utente proprietario);
    List<Wishlist> findByProprietarioAndVisibilita(Utente proprietario, String visibilita);

}
