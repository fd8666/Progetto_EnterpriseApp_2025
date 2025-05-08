package org.example.enterpriceappbackend.data.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enterpriceappbackend.data.entity.TagCategoria;
import org.example.enterpriceappbackend.data.repository.TagCategoriaRepository;
import org.example.enterpriceappbackend.data.service.TagCategoriaService;
import org.example.enterpriceappbackend.dto.TagCategoriaDTO;
import org.example.enterpriceappbackend.exceptions.BadRequest;
import org.example.enterpriceappbackend.exceptions.NotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TagCategoriaServiceImpl implements TagCategoriaService {

    private final TagCategoriaRepository tagCategoriaRepository;

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequest("ID non valido");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagCategoriaDTO> findAll() {
        List<TagCategoriaDTO> tagCategoriaDTOs = tagCategoriaRepository.findAll().stream()
                .map(this::toDto)
                .collect(toList());
        log.info("Trovate {} categorie", tagCategoriaDTOs.size());
        return tagCategoriaDTOs;
    }

    @Override
    @Transactional(readOnly = true)
    public TagCategoriaDTO findById(Long id) {
        validateId(id);
        TagCategoria tagCategoria = tagCategoriaRepository.findById(id)
                .orElseThrow(() -> new NotFound("Categoria non trovata con id: " + id));
        log.info("Categoria trovata con ID {}", tagCategoria.getId());
        return toDto(tagCategoria);
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
