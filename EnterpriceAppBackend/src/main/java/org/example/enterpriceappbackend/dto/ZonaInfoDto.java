package org.example.enterpriceappbackend.dto;

import lombok.Data;

@Data
public class ZonaInfoDto {  //per ottenere le zone cosi da fare una lista esplicita solo visiva per l'organizzatore

    private Long id;
    private String nome;
    private String descrizione;
    private int totalePosti;

}
