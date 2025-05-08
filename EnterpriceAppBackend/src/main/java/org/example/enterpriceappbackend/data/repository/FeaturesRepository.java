package org.example.enterpriceappbackend.data.repository;

import org.example.enterpriceappbackend.data.entity.Features;
import org.example.enterpriceappbackend.data.entity.TipoPosto;
import org.example.enterpriceappbackend.data.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface FeaturesRepository extends JpaRepository<Features, Long> , JpaSpecificationExecutor<Features> {

    Optional<Features> findByTipoPostoId(Long tipoPosto_id);

}
