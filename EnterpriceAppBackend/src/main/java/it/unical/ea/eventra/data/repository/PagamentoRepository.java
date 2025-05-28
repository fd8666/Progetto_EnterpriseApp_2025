package it.unical.ea.eventra.data.repository;

import it.unical.ea.eventra.data.entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    @Query("SELECT p FROM Pagamento p WHERE p.ordine.proprietario.id = :proprietarioId")
    List<Pagamento> findByProprietarioId(@Param("proprietarioId") Long id);
}
