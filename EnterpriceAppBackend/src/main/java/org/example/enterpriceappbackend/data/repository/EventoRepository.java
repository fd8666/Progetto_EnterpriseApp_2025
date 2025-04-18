package org.example.enterpriceappbackend.data.repository;

import org.example.enterpriceappbackend.data.entity.Evento;
import org.example.enterpriceappbackend.data.entity.Utente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository //metodi gia implementati grazie a JpaRepository
public interface EventoRepository extends JpaRepository<Evento, Long> , JpaSpecificationExecutor<Evento> {

    List<Evento> findAllByOrganizzatore(Utente organizzatore);
    List<Evento> findEventoByLuogo(String luogo);


}
