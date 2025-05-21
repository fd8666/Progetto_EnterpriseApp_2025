package org.example.enterpriceappbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.service.FeaturesService;
import org.example.enterpriceappbackend.dto.FeaturesDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/features")
@RequiredArgsConstructor
public class FeaturesController {

    private final FeaturesService featuresService;

    @GetMapping("/fromTipoPosto/{tipoPostoId}")
    public ResponseEntity<FeaturesDTO> getByTipoPostoId(@PathVariable Long tipoPostoId) {
        FeaturesDTO featureDTO = featuresService.getByTipoPostoId(tipoPostoId);
        return ResponseEntity.ok(featureDTO);
    }

    @PostMapping("/save")  //IMPORTANTE se vogliamo rendere l'ui piu fluida dobbiamo "inglobarlo" nel create i tipo posto quindi alla creazione della "fascia" si crea la features anche perche il modulo da compilare credo saranno insieme
    public ResponseEntity<FeaturesDTO> create(@Valid @RequestBody FeaturesDTO dto) {
        FeaturesDTO created = featuresService.create(dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}/update") //decidere
    public ResponseEntity<FeaturesDTO> update(@PathVariable Long id, @Valid @RequestBody FeaturesDTO dto) {
        FeaturesDTO updated = featuresService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

}
