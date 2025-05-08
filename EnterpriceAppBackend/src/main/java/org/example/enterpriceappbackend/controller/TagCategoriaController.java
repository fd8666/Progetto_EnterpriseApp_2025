package org.example.enterpriceappbackend.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.dto.TagCategoriaDTO;
import org.example.enterpriceappbackend.data.service.TagCategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/tagcategoria")
@RequiredArgsConstructor
@Api(value = "TagCategoria API", description = "Operazioni relative alla gestione delle categorie dei tag", tags = {"TagCategoria"})
public class TagCategoriaController {

    private final TagCategoriaService tagCategoriaService;

    @ApiOperation(value = "Recupera una TagCategoria per ID", notes = "Restituisce la `TagCategoriaDTO` corrispondente all'ID fornito.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Categoria trovata"),
            @ApiResponse(code = 404, message = "Categoria non trovata"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TagCategoriaDTO> findCategoriaById(@PathVariable Long id) {
        TagCategoriaDTO tagCategoriaDTO = tagCategoriaService.findById(id);
        return ResponseEntity.ok(tagCategoriaDTO);
    }

    @ApiOperation(value = "Recupera tutte le TagCategorie", notes = "Restituisce una lista di tutte le categorie dei tag disponibili.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Categorie recuperate con successo"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("")
    public ResponseEntity<List<TagCategoriaDTO>> findAllCategoria() {
        List<TagCategoriaDTO> tagcategoria = tagCategoriaService.findAll();
        return ResponseEntity.ok(tagcategoria);
    }
}
