package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.dto.TagCategoriaDTO;

public interface TagCategoriaService {
    TagCategoriaDTO create(TagCategoriaDTO TagCategoriaDto);
    void delete(Long id);

}
