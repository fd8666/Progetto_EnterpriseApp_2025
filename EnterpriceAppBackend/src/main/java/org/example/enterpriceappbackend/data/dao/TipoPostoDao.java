package org.example.enterpriceappbackend.data.dao;

import org.example.enterpriceappbackend.data.entity.TipoPosto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TipoPostoDao extends JpaRepository<TipoPosto, Long>, JpaSpecificationExecutor<TipoPosto> {
}
