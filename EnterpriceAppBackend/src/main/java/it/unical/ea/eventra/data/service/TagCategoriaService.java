package it.unical.ea.eventra.data.service;

import it.unical.ea.eventra.dto.TagCategoriaDTO;

import java.util.List;


public interface TagCategoriaService {
    List<TagCategoriaDTO> findAll();
    TagCategoriaDTO findById(Long id);


}
