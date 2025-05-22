package org.example.enterpriceappbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "TagCategoria", description = "Operazioni per la gestione delle categorie di tag")
public class TagCategoriaController {

    private final TagCategoriaService tagCategoriaService;

    @Operation(
            summary = "Recupera una categoria di tag tramite ID",
            description = "Restituisce una singola categoria di tag in base all'ID fornito"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria trovata con successo"),
            @ApiResponse(responseCode = "404", description = "Categoria non trovata")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TagCategoriaDTO> findCategoriaById(@PathVariable Long id) {
        TagCategoriaDTO tagCategoriaDTO = tagCategoriaService.findById(id);
        return ResponseEntity.ok(tagCategoriaDTO);
    }

    @Operation(
            summary = "Recupera tutte le categorie di tag",
            description = "Restituisce la lista completa di tutte le categorie di tag presenti nel sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista delle categorie restituita con successo")
    })
    @GetMapping("")
    public ResponseEntity<List<TagCategoriaDTO>> findAllCategoria() {
        List<TagCategoriaDTO> tagcategoria = tagCategoriaService.findAll();
        return ResponseEntity.ok(tagcategoria);
    }
}
