package org.example.enterpriceappbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BigliettoInfoDTO {
        private Long id;
        private String nomeSpettatore;
        private String cognomeSpettatore;
        private String emailSpettatore;
        private Long eventoId;
        private Long tipoPostoId;
        private Double prezzo;
        private Long pagamentoId;
        private LocalDateTime dataCreazione;


}