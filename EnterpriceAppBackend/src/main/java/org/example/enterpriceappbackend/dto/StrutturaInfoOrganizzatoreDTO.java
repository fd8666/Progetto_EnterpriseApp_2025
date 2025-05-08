package org.example.enterpriceappbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class StrutturaInfoOrganizzatoreDTO { //per la visione descrittiva della struttura da parte di organizzatore (compresa quindi descrizione e lista dettagliata zone)

    private Long id;
    private String nome;
    private String categoria;
    private String descrizione;
    private String immagine;
    private String indirizzo;

}
