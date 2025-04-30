package org.example.enterpriceappbackend.data.controller;


import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.TagCategoria;
import org.example.enterpriceappbackend.data.service.TagCategoriaService;
import org.example.enterpriceappbackend.dto.EventoDTO;
import org.example.enterpriceappbackend.dto.TagCategoriaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*",allowedHeaders = "*")
@RequestMapping("/api/tagcategoria")
@RequiredArgsConstructor
public class TagCategoriaController {
    private final TagCategoriaService tagCategoriaService;


    @GetMapping("/{id}")
    public ResponseEntity<TagCategoriaDTO> findCategoriaById(@PathVariable Long id) {
        TagCategoriaDTO tagCategoriaDTO = tagCategoriaService.findById(id);
        return new ResponseEntity<>(tagCategoriaDTO, HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<List<TagCategoriaDTO>> findAllCategoria() {
        List<TagCategoriaDTO> tagcategoria = tagCategoriaService.findAll();
        return new ResponseEntity<>(tagcategoria, HttpStatus.OK);
    }



}
