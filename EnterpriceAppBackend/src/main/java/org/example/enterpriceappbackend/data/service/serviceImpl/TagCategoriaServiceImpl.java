package org.example.enterpriceappbackend.data.service.serviceImpl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.TagCategoria;
import org.example.enterpriceappbackend.data.repository.TagCategoriaRepository;
import org.example.enterpriceappbackend.data.service.TagCategoriaService;
import org.example.enterpriceappbackend.dto.TagCategoriaDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional
public class TagCategoriaServiceImpl implements TagCategoriaService {

    private final TagCategoriaRepository tagCategoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TagCategoriaDTO> findAll() {
        return tagCategoriaRepository.findAll().stream()
                .map(this::toDto)
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TagCategoriaDTO findById(Long id) {
        return tagCategoriaRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Categoria non Trovata con id: " + id));
    }

    // -------------------- MAPPER INTERNO --------------------

    private TagCategoriaDTO toDto(TagCategoria tagCategoria) {
        TagCategoriaDTO dto = new TagCategoriaDTO();
        dto.setId(tagCategoria.getId());
        dto.setNome(tagCategoria.getNome());
        dto.setDescrizione(tagCategoria.getDescrizione());
        dto.setEventi(tagCategoria.getEventi());
        return dto;
    }

    private TagCategoria toEntity(TagCategoriaDTO tagCategoriaDTO) {
        TagCategoria tagCategoria = new TagCategoria();
        tagCategoria.setNome(tagCategoriaDTO.getNome());
        tagCategoria.setDescrizione(tagCategoriaDTO.getDescrizione());
        tagCategoria.setEventi(tagCategoriaDTO.getEventi());
        return tagCategoria;
    }
}
