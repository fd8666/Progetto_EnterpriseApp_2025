package it.unical.ea.eventra.dto;

import lombok.Data;

@Data
public class StrutturaInfoUtenteDTO { //per la visione delle strutture presenti da parte dell'utente
                                      // a cui non interessano:capienza/specifiche delle zone ma solo la loro posizione
    private Long id;                  //all'interno della zona descritta dall'immagine(piccola mappa grafica)
    private String nome;              //a parte verranno prese le coordinate per generare un pin su una mappa con altra api
    private String descrizione;
    private String immagine;
    private String indirizzo;

}
