package org.example.enterpriceappbackend.data.repository;

import org.example.enterpriceappbackend.data.entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findByOrdine_Proprietario(Long utenteId);
}
