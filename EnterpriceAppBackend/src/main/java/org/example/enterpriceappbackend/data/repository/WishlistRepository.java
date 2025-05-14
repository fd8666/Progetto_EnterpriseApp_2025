package org.example.enterpriceappbackend.data.repository;

import org.example.enterpriceappbackend.data.constants.Visibilita;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.entity.Wishlist;
import org.example.enterpriceappbackend.dto.WishlistCondivisaDTO;
import org.example.enterpriceappbackend.dto.WishlistDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository //metodi gia implementati grazie a JpaRepository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> , JpaSpecificationExecutor<Wishlist> {
    List<Wishlist>  findByUtenteId(Long utenteId);
    List<Wishlist> findByUtenteIdAndVisibilita(Long utenteId, Visibilita visibilita);
    Optional<Wishlist> findById(Long id);
    @Query("SELECT wc.wishlist FROM WishlistCondivisa wc WHERE wc.condivisaCon.id = :utenteId")
    List<Wishlist> findByCondivisaCon(@Param("utenteId") Long utenteId);


}
