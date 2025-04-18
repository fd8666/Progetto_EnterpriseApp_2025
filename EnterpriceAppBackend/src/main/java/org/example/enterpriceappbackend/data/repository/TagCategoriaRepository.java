package org.example.enterpriceappbackend.data.repository;

import org.example.enterpriceappbackend.data.entity.Evento;
import org.example.enterpriceappbackend.data.entity.TagCategoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository //metodi gia implementati grazie a JpaRepository
public interface TagCategoriaRepository extends JpaRepository<TagCategoria, Long> , JpaSpecificationExecutor<TagCategoria> {

    List<TagCategoria> findAllByEventi(List<Evento> eventi);
    List<TagCategoria> findAllByDescrizioneOrderByIdDesc(String descrizione);


}
