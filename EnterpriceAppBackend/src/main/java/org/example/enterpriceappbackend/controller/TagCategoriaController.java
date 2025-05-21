package org.example.enterpriceappbackend.controller;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.dto.TagCategoriaDTO;
import org.example.enterpriceappbackend.data.service.TagCategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/tag-categoria")
@RequiredArgsConstructor
public class TagCategoriaController {

    private final TagCategoriaService tagCategoriaService;

    @GetMapping("/{id}")
    public ResponseEntity<TagCategoriaDTO> findCategoriaById(@PathVariable Long id) {
        TagCategoriaDTO tagCategoriaDTO = tagCategoriaService.findById(id);
        return ResponseEntity.ok(tagCategoriaDTO);
    }

    @GetMapping("")
    public ResponseEntity<List<TagCategoriaDTO>> findAllCategoria() {
        List<TagCategoriaDTO> tagcategoria = tagCategoriaService.findAll();
        return ResponseEntity.ok(tagcategoria);
    }
}
