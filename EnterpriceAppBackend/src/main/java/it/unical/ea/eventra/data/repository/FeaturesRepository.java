package it.unical.ea.eventra.data.repository;

import it.unical.ea.eventra.data.entity.Features;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FeaturesRepository extends JpaRepository<Features, Long> , JpaSpecificationExecutor<Features> {

    Optional<Features> findByTipoPostoId(Long tipoPosto_id);

}
