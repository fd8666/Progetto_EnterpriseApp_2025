package it.unical.ea.eventra.data.repository;


import it.unical.ea.eventra.data.entity.TagCategoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository //metodi gia implementati grazie a JpaRepository
public interface TagCategoriaRepository extends JpaRepository<TagCategoria, Long> , JpaSpecificationExecutor<TagCategoria> {
    List<TagCategoria> findAll();
    TagCategoria findById(long id);


}
