package it.unical.ea.eventra.data.repository;

import it.unical.ea.eventra.data.constants.Visibilita;
import it.unical.ea.eventra.data.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
