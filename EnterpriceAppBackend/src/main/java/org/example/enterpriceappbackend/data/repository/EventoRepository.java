package org.example.enterpriceappbackend.data.repository;
import org.example.enterpriceappbackend.data.entity.Evento;
import org.example.enterpriceappbackend.data.entity.TagCategoria;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.dto.EventoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByOrganizzatore(Utente organizzatore);
    List<Evento> findByCategoria(TagCategoria categoria);
    List<Evento> findByDataOraAperturaCancelliAfter(LocalDateTime dataOra);
    List<Evento> findByNomeContainingIgnoreCase(String nome);
    List<Evento> findAll();
    List<Evento> findByLuogoContainingIgnoreCase(String luogo);
    List<Evento> findByDataOraAperturaCancelliBetween(LocalDateTime dataInizio, LocalDateTime dataFine);

}