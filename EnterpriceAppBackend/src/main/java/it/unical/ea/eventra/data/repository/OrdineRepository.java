package it.unical.ea.eventra.data.repository;

import it.unical.ea.eventra.data.entity.Ordine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdineRepository extends JpaRepository<Ordine, Long> {
    List<Ordine> findByProprietarioId(Long proprietarioId);
}
