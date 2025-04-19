package org.example.enterpriceappbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BigliettoInfoDto {//per la visualizzazione del biglietto semplice magari in una lista in cui
                               // voglio i principali attributi e nome evento ecc li prendo tramite l'id
    private Long id;
    private String nomeSpettatore;
    private String cognomeSpettatore;
    private String emailSpettatore;

    private Long eventoId;
    private Long postoId;
    private Long pagamentoId;

    private LocalDateTime dataCreazione;

}
