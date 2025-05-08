package org.example.enterpriceappbackend.data.repository;


import org.example.enterpriceappbackend.data.entity.Struttura;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StrutturaRepository extends JpaRepository<Struttura, Long>{

    //per ottenere le strutture da parte dell'utente quando ne cerca una specifica dell'evento
    Optional<Struttura> findById(Long id);
    List<Struttura> findByNomeContainingIgnoreCase(String nome);
    List<Struttura> findByIndirizzoContainingIgnoreCase(String indirizzo);
    List<Struttura> findByCategoria(String categoria);

    //ricerca strutture organizzatore (Lui potra solo organizzare un evento nelle strutture convenzionate)
    long countByCategoria(String categoria);

    //ricerca organizzatore per filtri
    List<Struttura> findAll(Specification<Struttura> spec);

    //tutte le categorie di struttura al momento nel db magari per menu a tendina da cui selezionare nella ricerca
    @Query("SELECT DISTINCT s.categoria FROM Struttura s WHERE s.categoria IS NOT NULL")
    List<String> findAllCategorieDistinct();

    //ricerca utente struttura per evento
    @Query("SELECT s FROM Struttura s JOIN s.eventi e WHERE e.id = :eventoId")
    Struttura findByEventoId(@Param("eventoId") Long eventoId);

}
