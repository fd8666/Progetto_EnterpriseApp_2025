package org.example.enterpriceappbackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(value = "Struttura API", description = "Operazioni relative alla gestione delle strutture", tags = {"Struttura"})
public class StrutturaController {

    private final StrutturaService strutturaService;

    @ApiOperation(
            value = "Ottieni struttura per ID",
            notes = "Recupera una struttura completa utilizzando l'ID specificato. "
                    + "Restituisce l'entità `Struttura` completa, inclusi tutti i campi disponibili senza limitazioni tramite DTO.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Struttura trovata"),
            @ApiResponse(code = 404, message = "Struttura non trovata con l'ID specificato"),
            @ApiResponse(code = 401, message = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/id/{id}")
    public ResponseEntity<Struttura> getById(@PathVariable Long id) {
        Struttura struttura = strutturaService.getById(id);
        return ResponseEntity.ok(struttura);
    }


    @ApiOperation(
            value = "Ottieni strutture per nome",
            notes = "Cerca strutture con nome contenente il testo specificato (match parziale). "
                    + "Restituisce una lista di oggetti `Struttura` completi. Richiede almeno 3 caratteri per il parametro `nome`.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Strutture trovate"),
            @ApiResponse(code = 400, message = "Il nome da cercare deve contenere almeno 3 caratteri"),
            @ApiResponse(code = 404, message = "Nessuna struttura trovata con il nome specificato"),
            @ApiResponse(code = 401, message = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Struttura>> getByNome(@PathVariable String nome) {
        List<Struttura> strutture = strutturaService.getByNome(nome);
        return ResponseEntity.ok(strutture);
    }


    @ApiOperation(
            value = "Ottieni strutture per indirizzo",
            notes = "Cerca strutture in base all'indirizzo, utilizzando una ricerca parziale. "
                    + "Restituisce una lista di oggetti `Struttura`. L'indirizzo deve contenere almeno 5 caratteri.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Strutture trovate"),
            @ApiResponse(code = 400, message = "L'indirizzo da cercare deve contenere almeno 5 caratteri"),
            @ApiResponse(code = 404, message = "Nessuna struttura trovata all'indirizzo specificato"),
            @ApiResponse(code = 401, message = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/indirizzo/{indirizzo}")
    public ResponseEntity<List<Struttura>> getByIndirizzo(@PathVariable String indirizzo) {
        List<Struttura> strutture = strutturaService.getByIndirizzo(indirizzo);
        return ResponseEntity.ok(strutture);
    }


    @ApiOperation(
            value = "Ottieni tutte le categorie",
            notes = "Recupera tutte le categorie disponibili tra le strutture presenti nel sistema. "
                    + "Restituisce una lista di stringhe (`List<String>`).")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Categorie trovate"),
            @ApiResponse(code = 404, message = "Nessuna categoria trovata nel sistema"),
            @ApiResponse(code = 401, message = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/categorie")
    public ResponseEntity<List<String>> getCategorie() {
        List<String> categorie = strutturaService.getAllCategorie();
        return ResponseEntity.ok(categorie);
    }


    // /strutture/categoria/{categoria}
    @ApiOperation(
            value = "Ottieni strutture per categoria",
            notes = "Restituisce una lista di strutture che appartengono alla categoria specificata. "
                    + "La ricerca è esatta sul nome della categoria essendo selezionato da interfaccia e non inserito manualmente. Il risultato contiene entità `Struttura` complete.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Strutture trovate"),
            @ApiResponse(code = 404, message = "Nessuna struttura trovata per la categoria specificata"),//non possono esserci bad request
            @ApiResponse(code = 401, message = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Struttura>> getByCategoria(@PathVariable String categoria) {
        List<Struttura> strutture = strutturaService.getByCategoria(categoria);
        return ResponseEntity.ok(strutture);
    }


    // /strutture/vategoria/{categoria}/count
    @ApiOperation(
            value = "Conteggio strutture per categoria",
            notes = "Restituisce il numero totale di strutture appartenenti a una categoria specificata.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Conteggio riuscito"),
            @ApiResponse(code = 404, message = "Nessuna struttura trovata per la categoria specificata"),
            @ApiResponse(code = 401, message = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/categoria/{categoria}/count")
    public ResponseEntity<Long> countByCategoria(@PathVariable String categoria) {
        Long count = strutturaService.countByCategoria(categoria);
        return ResponseEntity.ok(count);
    }


    // /strutture/{id}/utente
    @ApiOperation(
            value = "Ottieni informazioni per l'utente",
            notes = "Restituisce un oggetto `StrutturaInfoUtenteDTO`, contenente un sottoinsieme di dati pubblicabili a utenti generici (clienti). "
                    + "Include ad esempio nome, categoria, indirizzo, e coordinate approssimative.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Informazioni utente trovate"),
            @ApiResponse(code = 404, message = "Struttura non trovata con l'ID specificato"),
            @ApiResponse(code = 401, message = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/{id}/infoUtente")
    public ResponseEntity<StrutturaInfoUtenteDTO> getInfoUtente(@PathVariable Long id) {
        StrutturaInfoUtenteDTO dto = strutturaService.getInfoForUtenteById(id);
        return ResponseEntity.ok(dto);
    }


    // /strutture/{id}/map
    @ApiOperation(
            value = "Ottieni informazioni mappa",
            notes = "Restituisce un oggetto `StrutturaMapInfoDTO`, contenente coordinate geografiche (latitudine e longitudine) della struttura. "
                    + "Utile per generare pin su mappe nel frontend.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Informazioni mappa trovate"),
            @ApiResponse(code = 404, message = "Impossibile recuperare le coordinate per la struttura"),
            @ApiResponse(code = 401, message = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/{id}/map")
    public ResponseEntity<StrutturaMapInfoDTO> getMap(@PathVariable Long id) {
        StrutturaMapInfoDTO mapInfo = strutturaService.getMapInfoById(id);
        return ResponseEntity.ok(mapInfo);
    }


    // /strutture/{id}/organizzatore
    @ApiOperation(
            value = "Ottieni informazioni per l'organizzatore",
            notes = "Restituisce un oggetto `StrutturaInfoOrganizzatoreDTO` contenente dati 'estesi' di un astruttura per utente organizzatore ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Informazioni organizzatore trovate"),
            @ApiResponse(code = 404, message = "Struttura non trovata con l'ID specificato"),
            @ApiResponse(code = 401, message = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/{id}/infoOrganizzatore")
    public ResponseEntity<StrutturaInfoOrganizzatoreDTO> getOrganizzatoreById(@PathVariable Long id) {
        StrutturaInfoOrganizzatoreDTO dto = strutturaService.getOrganizzatoreDTO(id);
        return ResponseEntity.ok(dto);
    }


    // /strutture/listaOrganizzatore
    @ApiOperation(
            value = "Ottieni tutte le strutture per l'organizzatore",
            notes = "Restituisce una lista di `StrutturaInfoOrganizzatoreDTO` per tutte le strutture convenzionate. "
                    + "I dati restituiti sono pensati per essere mostrati in un'interfaccia che mostri una lista da cui scegliere.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Lista delle strutture restituita con successo"),
            @ApiResponse(code = 401, message = "Non autorizzato"),
            @ApiResponse(code = 404, message = "Nessuna struttura disponibile"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/strutture/listaOrganizzatore")
    public ResponseEntity<List<StrutturaInfoOrganizzatoreDTO>> listaOrganizzatore() {
        List<StrutturaInfoOrganizzatoreDTO> lista = strutturaService.getAllForOrganizzatore();
        return ResponseEntity.ok(lista);
    }


    // GET /strutture/evento/{eventoId}
    @ApiOperation(
            value = "Ottieni struttura tramite ID evento",
            notes = "Recupera una struttura a partire dall'ID di un evento che si svolge al suo interno. "
                    + "Restituisce un `StrutturaInfoUtenteDTO` con le informazioni pubblicabili della struttura.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Struttura trovata e restituita correttamente"),
            @ApiResponse(code = 401, message = "Non autorizzato"),
            @ApiResponse(code = 404, message = "Nessuna struttura trovata per l'evento specificato"),
            @ApiResponse(code = 500, message = "Errore interno del server")
    })
    @GetMapping("/strutture/evento/{eventoId}")
    public ResponseEntity<StrutturaInfoUtenteDTO> getByEvento(@PathVariable Long eventoId) {
        StrutturaInfoUtenteDTO dto = strutturaService.getStrutturaByEvento(eventoId);
        return ResponseEntity.ok(dto);
    }


    // POST /strutture/filtra-organizzatore
    @ApiOperation(
            value = "Filtra strutture per organizzatore",
            notes = "Permette di filtrare le strutture utilizzando parametri opzionali: nome (min 3 caratteri), categoria (da scegliere) e indirizzo (min 5 caratteri). "
                    + "Restituisce una lista di `StrutturaInfoOrganizzatoreDTO` in base ai criteri indicati.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Strutture filtrate con successo"),
            @ApiResponse(code = 400, message = "Bad Request: uno dei parametri non soddisfa le condizioni minime (nome o indirizzo troppo corti)"),
            @ApiResponse(code = 404, message = "Nessuna struttura trovata con i criteri forniti"),
            @ApiResponse(code = 401, message = "Non autorizzato. L'utente non ha i permessi necessari."),
            @ApiResponse(code = 500, message = "Errore interno del server")
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
