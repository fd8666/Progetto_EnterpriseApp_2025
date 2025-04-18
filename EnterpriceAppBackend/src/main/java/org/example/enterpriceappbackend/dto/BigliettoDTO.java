package org.example.enterpriceappbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BigliettoDTO {
    private Long id;
    private String nomeSpettatore;
    private String cognomeSpettatore;
    private String emailSpettatore;
    private LocalDate dataCreazione;
    private Long eventoId;
    private Long zonaId;
    private Long pagamentoId;
}
