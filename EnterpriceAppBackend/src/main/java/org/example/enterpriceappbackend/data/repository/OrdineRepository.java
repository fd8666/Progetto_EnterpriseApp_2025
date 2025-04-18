package org.example.enterpriceappbackend.data.repository;

import org.example.enterpriceappbackend.data.entity.Ordine;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdineRepository extends JpaRepository<Ordine, Long> {
    List<Ordine> findByProprietario(Utente proprietario);
}
