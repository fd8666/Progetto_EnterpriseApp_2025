package it.unical.ea.eventra.data.repository;
import it.unical.ea.eventra.data.entity.Evento;
import it.unical.ea.eventra.data.entity.TagCategoria;
import it.unical.ea.eventra.data.entity.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
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
    List<Evento> findByDataOraEventoBeforeAndDeletedEquals(LocalDateTime dataOra, Integer deleted);
}