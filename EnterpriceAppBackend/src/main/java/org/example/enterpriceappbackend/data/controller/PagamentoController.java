package org.example.enterpriceappbackend.data.controller;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Pagamento;
import org.example.enterpriceappbackend.data.service.PagamentoService;
import org.example.enterpriceappbackend.dto.PagamentoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamenti")
@RequiredArgsConstructor

public class PagamentoController {

    private final PagamentoService pagamentoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PagamentoDTO>> getAllPagamenti() {
        return ResponseEntity.ok(pagamentoService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagamentoDTO> getPagamentoById(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.findById(id));
    }

    @GetMapping("/utente/{utenteId")
    public ResponseEntity<List<PagamentoDTO>> getPagamentoByUtente(@PathVariable Long utenteId) {
        return ResponseEntity.ok(pagamentoService.findByUtenteId(utenteId));
    }

    @PostMapping
    public ResponseEntity<PagamentoDTO> savePagamento(@PathVariable PagamentoDTO pagamentodto) {
        return ResponseEntity.ok(pagamentoService.save(pagamentodto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagamentoDTO> updatePagamento(@PathVariable Long id,@RequestBody PagamentoDTO pagamentodto){
        return ResponseEntity.ok(pagamentoService.update(id,pagamentodto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePagamento(@PathVariable Long id){
        pagamentoService.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
