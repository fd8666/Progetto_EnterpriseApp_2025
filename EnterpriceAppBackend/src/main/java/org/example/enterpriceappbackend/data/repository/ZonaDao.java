package org.example.enterpriceappbackend.data.repository;

import org.example.enterpriceappbackend.data.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZonaDao extends JpaRepository<Zona, Long>{

    Optional<Zona> findById(Long id);

    //zone della struttura da mostrare dopo la selezione di una struttura da parte di un organizzatore
    //l'interfaccia di visualizzazione delle strutture con le zone sara separata da quella di scelta di una struttura per un nuovo evento
    @Query("select z from Zona z where z.struttura.id=:strutturaId")
    List<Zona> findAllByStrutturaId(Long strutturaId);

}
