package org.example.enterpriceappbackend.data.dao;

import org.example.enterpriceappbackend.data.entity.Evento;
import org.example.enterpriceappbackend.data.entity.Utente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoDao extends JpaRepository<Evento, Long> , JpaSpecificationExecutor<Evento> {

    List<Evento> findAllByOrganizzatore(Utente organizzatore);
    List<Evento> findAllByUtente(Utente utente);
    List<Evento> findAllByEvento(Evento evento);
    List<Evento> findEventoByLuogo(String luogo);


}
