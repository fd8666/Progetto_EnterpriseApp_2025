package it.unical.ea.eventra.data.repository;

import it.unical.ea.eventra.data.entity.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtenteRepository  extends JpaRepository<Utente, Long> {

    Optional<Utente> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Utente> findAll();
    void deleteUtenteById(Long id);
}
