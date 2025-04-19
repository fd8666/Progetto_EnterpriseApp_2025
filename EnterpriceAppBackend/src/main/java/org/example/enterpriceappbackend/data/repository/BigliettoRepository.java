package org.example.enterpriceappbackend.data.repository;


import org.example.enterpriceappbackend.data.entity.Biglietto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BigliettoRepository extends JpaRepository<Biglietto, Long> {

    Optional<Biglietto> findById(Long id);

}
