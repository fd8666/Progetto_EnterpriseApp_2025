package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.dto.TagCategoriaDto;

public interface TagCategoriaService {
    TagCategoriaDto create(TagCategoriaDto TagCategoriaDto);
    void delete(Long id);

}
