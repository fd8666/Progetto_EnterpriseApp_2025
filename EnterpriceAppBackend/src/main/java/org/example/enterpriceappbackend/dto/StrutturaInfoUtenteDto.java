package org.example.enterpriceappbackend.dto;

import lombok.Data;

@Data
public class StrutturaInfoUtenteDto { //per la visione delle strutture presenti da parte dell'utente
                                      // a cui non interessano:capienza/specifiche delle zone ma solo la loro posizione
    private Long id;                  //all'interno della zona descritta dall'immagine(piccola mappa grafica)
    private String nome;
    private String descrizione;

}
