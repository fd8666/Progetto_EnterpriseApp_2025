package org.example.enterpriceappbackend.data.repository;

import org.example.enterpriceappbackend.data.entity.TipoPosto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TipoPostoRepository extends JpaRepository<TipoPosto, Long>, JpaSpecificationExecutor<TipoPosto> {
}
