package org.example.enterpriceappbackend.data.repository;


import org.example.enterpriceappbackend.data.entity.Struttura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StrutturaRepository extends JpaRepository<Struttura, Long>{

    //per ottenere le strutture da parte dell'utente quando ne cerca una specifica dell'evento
    Optional<Struttura> findById(Long id);
    Optional<Struttura> findByNome(String nome);
    Optional<Struttura> findByCategoria(String categoria);
    Optional<Struttura> findByIndirizzo(String indirizzo);

    //ricerca strutture organizzatore (Lui potra solo organizzare un evento nelle strutture convenzionate)
    int countByCategoria(String categoria);//in caso quando l'organizzatore vuole vedere strutture per una determinata categoria ossiamo mostrare quante sono

    //ricerca organizzatore per filtri
    Page<Struttura> findAll(Specification<Struttura> spec, Pageable pageable);

    //tutte le categorie di struttura al momento nel db magari per menu a tendina da cui selezionare nella ricerca
    @Query("SELECT DISTINCT s.categoria FROM Struttura s WHERE s.categoria IS NOT NULL")
    List<String> findAllCategorieDistinct();

    //altre aggiunte da decidere

}
