package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.dto.TagCategoriaDTO;

import java.util.List;


public interface TagCategoriaService {
    List<TagCategoriaDTO> findAll();
    TagCategoriaDTO findById(Long id);
    //Metodi CRUD
    //TagCategoriaDTO create(TagCategoriaDTO tagCategoriaDTO);
    //TagCategoriaDTO save(TagCategoriaDTO tagCategoriaDTO);
    //TagCategoriaDTO update(Long id, TagCategoriaDTO tagCategoriaDTO);
    //void deleteById(Long id);

}
