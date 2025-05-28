package it.unical.ea.eventra.data.repository;

import it.unical.ea.eventra.data.entity.TipoPosto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TipoPostoRepository extends JpaRepository<TipoPosto, Long> {

    //Query per avere la somma di tutti i tipoPosto di un determinato evento
    @Query("SELECT SUM(t.postiDisponibili) FROM TipoPosto t WHERE t.evento.id = :eventoId")
    Optional<Integer> sumPostiByEvento(@Param("eventoId") Long eventoId);

    //Query per trovare tipo posto per eventoId
    @Query("SELECT t FROM TipoPosto t JOIN FETCH t.evento e WHERE e.id = :eventoId")
    List<TipoPosto> findByEventoId(@Param("eventoId") Long eventoId);

    // Query per trovare tipo posto per prezzo
    @Query("SELECT t FROM TipoPosto t WHERE t.prezzo = :prezzo AND t.postiDisponibili > 0")
    Optional<TipoPosto> findByPrezzo(@Param("prezzo") Double prezzo);
}