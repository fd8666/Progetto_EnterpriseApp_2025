// BigliettoRepository.java
package it.unical.ea.eventra.data.repository;

import it.unical.ea.eventra.data.entity.Biglietto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BigliettoRepository extends JpaRepository<Biglietto, Long> {
    List<Biglietto> findByEventoId(Long eventoId);
    List<Biglietto> findByTipoPostoId(Long tipoPostoId);
    List<Biglietto> findByPagamentoOrdineProprietarioId(Long utenteId);
}