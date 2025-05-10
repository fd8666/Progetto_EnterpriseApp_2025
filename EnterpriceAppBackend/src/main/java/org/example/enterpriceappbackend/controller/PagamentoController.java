package org.example.enterpriceappbackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.service.PagamentoService;
import org.example.enterpriceappbackend.dto.PagamentoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamenti")
@RequiredArgsConstructor
@Api(value = "Pagamenti API", description = "Operazioni relative alla gestione dei Pagamenti", tags = {"Pagamenti"})
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @ApiOperation(value = "Recupera i pagamenti di un utente")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Pagamenti trovati con successo"),
            @ApiResponse(code = 400, message = "Dati non validi"),
            @ApiResponse(code = 404, message = "Utente non trovato")
    })
    @GetMapping("/utente/{utenteId}")
    public ResponseEntity<List<PagamentoDTO>> getPagamentoByUtente(@PathVariable Long utenteId) {
        return ResponseEntity.ok(pagamentoService.findByUtenteId(utenteId));
    }

    @ApiOperation(value = "Crea un nuovo pagamento")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Pagamento creato con successo"),
            @ApiResponse(code = 400, message = "Richiesta non valida")
    })
    @PostMapping
    public ResponseEntity<PagamentoDTO> createPagamento(@RequestBody PagamentoDTO pagamentoDTO) {
        PagamentoDTO creato = pagamentoService.create(pagamentoDTO);
        return ResponseEntity.ok(creato);
    }

    @ApiOperation(value = "Aggiorna un pagamento (solo admin)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Pagamento aggiornato con successo"),
            @ApiResponse(code = 400, message = "Dati non validi"),
            @ApiResponse(code = 403, message = "Accesso negato"),
            @ApiResponse(code = 404, message = "Pagamento non trovato")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagamentoDTO> updatePagamento(@PathVariable Long id, @RequestBody PagamentoDTO pagamentodto){
        return ResponseEntity.ok(pagamentoService.update(id, pagamentodto));
    }

    @ApiOperation(value = "Elimina un pagamento (solo admin)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Pagamento eliminato con successo"),
            @ApiResponse(code = 400, message = "Dati non validi"),
            @ApiResponse(code = 403, message = "Accesso negato"),
            @ApiResponse(code = 404, message = "Pagamento non trovato")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePagamento(@PathVariable Long id){
        pagamentoService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
