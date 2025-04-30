package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.dto.TagCategoriaDTO;

import java.util.List;


public interface TagCategoriaService {
    List<TagCategoriaDTO> findAll();
    TagCategoriaDTO findById(Long id);


}
