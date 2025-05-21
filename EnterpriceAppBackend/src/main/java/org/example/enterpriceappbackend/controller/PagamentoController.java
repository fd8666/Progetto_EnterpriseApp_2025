package org.example.enterpriceappbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.service.PagamentoService;
import org.example.enterpriceappbackend.dto.PagamentoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamenti")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;


    @GetMapping("/{utenteId}") //funzionante
    public ResponseEntity<List<PagamentoDTO>> getPagamentoByUtente(@PathVariable Long utenteId) {
        return ResponseEntity.ok(pagamentoService.findByUtenteId(utenteId));
    }


    @PostMapping("/createPagamento/{ordineId}")
    public ResponseEntity<PagamentoDTO> createPagamento(
            @RequestBody @Valid PagamentoDTO pagamentoDTO,
            @PathVariable Long ordineId) {
        PagamentoDTO pagamento = pagamentoService.createPagamento(ordineId,pagamentoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(pagamento);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagamentoDTO> updatePagamento(@PathVariable Long id, @RequestBody PagamentoDTO pagamentodto){
        return ResponseEntity.ok(pagamentoService.update(id, pagamentodto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePagamento(@PathVariable Long id){
        pagamentoService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
