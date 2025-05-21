package org.example.enterpriceappbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Struttura;
import org.example.enterpriceappbackend.data.service.StrutturaService;
import org.example.enterpriceappbackend.dto.StrutturaInfoOrganizzatoreDTO;
import org.example.enterpriceappbackend.dto.StrutturaInfoUtenteDTO;
import org.example.enterpriceappbackend.dto.StrutturaMapInfoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*",allowedHeaders = "*")
@RequestMapping("/api/strutture")
@RequiredArgsConstructor
@Tag(name = "Struttura", description = "Operazioni relative alla gestione delle strutture")
public class StrutturaController {

    private final StrutturaService strutturaService;

    @Operation(
            summary = "Ottieni struttura per ID",
            description = "Recupera una struttura completa utilizzando l'ID specificato. Restituisce l'entità `Struttura` completa, inclusi tutti i campi disponibili senza limitazioni tramite DTO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Struttura trovata"),
            @ApiResponse(responseCode = "404", description = "Struttura non trovata con l'ID specificato"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/id/{id}")
    public ResponseEntity<Struttura> getById(@PathVariable Long id) {
        Struttura struttura = strutturaService.getById(id);
        return ResponseEntity.ok(struttura);
    }


    @Operation(
            summary = "Ottieni strutture per nome",
            description = "Cerca strutture con nome contenente il testo specificato (match parziale). Richiede almeno 3 caratteri per il parametro `nome`.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Strutture trovate"),
            @ApiResponse(responseCode = "400", description = "Il nome da cercare deve contenere almeno 3 caratteri"),
            @ApiResponse(responseCode = "404", description = "Nessuna struttura trovata con il nome specificato"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Struttura>> getByNome(@PathVariable String nome) {
        List<Struttura> strutture = strutturaService.getByNome(nome);
        return ResponseEntity.ok(strutture);
    }


    @Operation(
            summary = "Ottieni strutture per indirizzo",
            description = "Cerca strutture in base all'indirizzo, utilizzando una ricerca parziale. L'indirizzo deve contenere almeno 5 caratteri.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Strutture trovate"),
            @ApiResponse(responseCode = "400", description = "L'indirizzo da cercare deve contenere almeno 5 caratteri"),
            @ApiResponse(responseCode = "404", description = "Nessuna struttura trovata all'indirizzo specificato"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/indirizzo/{indirizzo}")
    public ResponseEntity<List<Struttura>> getByIndirizzo(@PathVariable String indirizzo) {
        List<Struttura> strutture = strutturaService.getByIndirizzo(indirizzo);
        return ResponseEntity.ok(strutture);
    }


    @Operation(
            summary = "Ottieni tutte le categorie",
            description = "Recupera tutte le categorie disponibili tra le strutture presenti nel sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorie trovate"),
            @ApiResponse(responseCode = "404", description = "Nessuna categoria trovata nel sistema"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/categorie")
    public ResponseEntity<List<String>> getCategorie() {
        List<String> categorie = strutturaService.getAllCategorie();
        return ResponseEntity.ok(categorie);
    }


    // /strutture/categoria/{categoria}
    @Operation(
            summary = "Ottieni strutture per categoria",
            description = "Restituisce una lista di strutture che appartengono alla categoria specificata. La ricerca è esatta sul nome della categoria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Strutture trovate"),
            @ApiResponse(responseCode = "404", description = "Nessuna struttura trovata per la categoria specificata"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Struttura>> getByCategoria(@PathVariable String categoria) {
        List<Struttura> strutture = strutturaService.getByCategoria(categoria);
        return ResponseEntity.ok(strutture);
    }


    // /strutture/vategoria/{categoria}/count
    @Operation(
            summary = "Conteggio strutture per categoria",
            description = "Restituisce il numero totale di strutture appartenenti a una categoria specificata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conteggio riuscito"),
            @ApiResponse(responseCode = "404", description = "Nessuna struttura trovata per la categoria specificata"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/categoria/{categoria}/count")
    public ResponseEntity<Long> countByCategoria(@PathVariable String categoria) {
        Long count = strutturaService.countByCategoria(categoria);
        return ResponseEntity.ok(count);
    }


    // /strutture/{id}/utente
    @Operation(
            summary = "Ottieni informazioni per l'utente",
            description = "Restituisce un oggetto `StrutturaInfoUtenteDTO` contenente dati pubblicabili a utenti generici come nome, categoria, indirizzo e coordinate approssimative.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informazioni utente trovate"),
            @ApiResponse(responseCode = "404", description = "Struttura non trovata con l'ID specificato"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/{id}/infoUtente")
    public ResponseEntity<StrutturaInfoUtenteDTO> getInfoUtente(@PathVariable Long id) {
        StrutturaInfoUtenteDTO dto = strutturaService.getInfoForUtenteById(id);
        return ResponseEntity.ok(dto);
    }


    // /strutture/{id}/map
    @Operation(
            summary = "Ottieni informazioni mappa",
            description = "Restituisce un oggetto `StrutturaMapInfoDTO` con coordinate geografiche della struttura, utile per mappe frontend.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informazioni mappa trovate"),
            @ApiResponse(responseCode = "404", description = "Impossibile recuperare le coordinate per la struttura"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/{id}/map")
    public ResponseEntity<StrutturaMapInfoDTO> getMap(@PathVariable Long id) {
        StrutturaMapInfoDTO mapInfo = strutturaService.getMapInfoById(id);
        return ResponseEntity.ok(mapInfo);
    }


    // /strutture/{id}/organizzatore
    @Operation(
            summary = "Ottieni informazioni per l'organizzatore",
            description = "Restituisce un oggetto `StrutturaInfoOrganizzatoreDTO` contenente dati estesi di una struttura per utente organizzatore.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informazioni organizzatore trovate"),
            @ApiResponse(responseCode = "404", description = "Struttura non trovata con l'ID specificato"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/{id}/infoOrganizzatore")
    public ResponseEntity<StrutturaInfoOrganizzatoreDTO> getOrganizzatoreById(@PathVariable Long id) {
        StrutturaInfoOrganizzatoreDTO dto = strutturaService.getOrganizzatoreDTO(id);
        return ResponseEntity.ok(dto);
    }


    // /strutture/listaOrganizzatore
    @Operation(
            summary = "Ottieni tutte le strutture per l'organizzatore",
            description = "Restituisce una lista di `StrutturaInfoOrganizzatoreDTO` per tutte le strutture convenzionate.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista delle strutture restituita con successo"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "404", description = "Nessuna struttura disponibile"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/listaOrganizzatore")
    public ResponseEntity<List<StrutturaInfoOrganizzatoreDTO>> listaOrganizzatore() {
        List<StrutturaInfoOrganizzatoreDTO> lista = strutturaService.getAllForOrganizzatore();
        return ResponseEntity.ok(lista);
    }


    // GET /strutture/evento/{eventoId}
    @Operation(
            summary = "Ottieni struttura tramite ID evento",
            description = "Recupera una struttura a partire dall'ID di un evento. Restituisce un `StrutturaInfoUtenteDTO` con informazioni pubblicabili.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Struttura trovata e restituita correttamente"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato"),
            @ApiResponse(responseCode = "404", description = "Nessuna struttura trovata per l'evento specificato"),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<StrutturaInfoUtenteDTO> getByEvento(@PathVariable Long eventoId) {
        StrutturaInfoUtenteDTO dto = strutturaService.getStrutturaByEvento(eventoId);
        return ResponseEntity.ok(dto);
    }


    // POST /strutture/filtra-organizzatore
    @Operation(
            summary = "Filtra strutture per organizzatore",
            description = "Permette di filtrare le strutture utilizzando parametri opzionali: nome (min 3 caratteri), categoria e indirizzo (min 5 caratteri).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Strutture filtrate con successo"),
            @ApiResponse(responseCode = "400", description = "Bad Request: uno dei parametri non soddisfa le condizioni minime"),
            @ApiResponse(responseCode = "404", description = "Nessuna struttura trovata con i criteri forniti"),
            @ApiResponse(responseCode = "401", description = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping("/filtra-organizzatore")
    public ResponseEntity<List<StrutturaInfoOrganizzatoreDTO>> filtraOrganizzatore(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String indirizzo) {
        List<StrutturaInfoOrganizzatoreDTO> risultati = strutturaService.filtraStrutture(nome, categoria, indirizzo);
        return ResponseEntity.ok(risultati);
    }

}
