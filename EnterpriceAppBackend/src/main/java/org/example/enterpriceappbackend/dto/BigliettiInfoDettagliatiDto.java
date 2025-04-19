package org.example.enterpriceappbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BigliettiInfoDettagliatiDto {//per una schermata dedicata olo ad un biglietto in cui ci sono tutte le info piu ampie
                                          // complete come evento posizione ecc e tipoposto con features descrizione ecc non solo un nome
    private Long id;
    private String nomeSpettatore;
    private String cognomeSpettatore;
    private String emailSpettatore;

    private EventoDto evento;  // Dettagli completi dell'evento
    //private TipoPostoDto tipoPosto; // Dettagli completi del tipo di posto

    private Long pagamentoId;

    private LocalDateTime dataCreazione;

}
