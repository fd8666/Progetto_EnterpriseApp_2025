package it.unical.ea.eventra.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import it.unical.ea.eventra.data.service.PagamentoService;
import it.unical.ea.eventra.dto.PagamentoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamenti")
@RequiredArgsConstructor
@Tag(name = "Pagamento", description = "Gestione dei pagamenti (creazione, aggiornamento, cancellazione, recupero)")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @Operation(
            summary = "Recupera i pagamenti per utente",
            description = "Restituisce una lista di pagamenti effettuati da un determinato utente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista dei pagamenti restituita con successo"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato")
    })
    @GetMapping("/{utenteId}")
    public ResponseEntity<List<PagamentoDTO>> getPagamentoByUtente(@PathVariable Long utenteId) {
        return ResponseEntity.ok(pagamentoService.findByUtenteId(utenteId));
    }

    @Operation(
            summary = "Crea un nuovo pagamento",
            description = "Crea un pagamento associato a un ordine specifico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pagamento creato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati del pagamento non validi"),
            @ApiResponse(responseCode = "404", description = "Ordine non trovato")
    })
    @PostMapping("/createPagamento/{ordineId}")
    public ResponseEntity<PagamentoDTO> createPagamento(
            @RequestBody @Valid PagamentoDTO pagamentoDTO,
            @PathVariable Long ordineId) {
        PagamentoDTO pagamento = pagamentoService.createPagamento(ordineId, pagamentoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(pagamento);
    }

    @Operation(
            summary = "Aggiorna un pagamento",
            description = "Aggiorna i dati di un pagamento esistente. Accesso consentito solo agli ADMIN"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamento aggiornato con successo"),
            @ApiResponse(responseCode = "403", description = "Accesso non autorizzato"),
            @ApiResponse(responseCode = "404", description = "Pagamento non trovato")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagamentoDTO> updatePagamento(@PathVariable Long id, @RequestBody PagamentoDTO pagamentodto){
        return ResponseEntity.ok(pagamentoService.update(id, pagamentodto));
    }

    @Operation(
            summary = "Elimina un pagamento",
            description = "Elimina un pagamento esistente in base al suo ID. Accesso riservato agli ADMIN"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamento eliminato con successo"),
            @ApiResponse(responseCode = "403", description = "Accesso non autorizzato"),
            @ApiResponse(responseCode = "404", description = "Pagamento non trovato")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePagamento(@PathVariable Long id){
        pagamentoService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
