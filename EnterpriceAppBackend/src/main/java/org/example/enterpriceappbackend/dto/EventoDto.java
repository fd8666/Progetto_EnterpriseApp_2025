package org.example.enterpriceappbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Evento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoDto {

    private Long id;
    private String nome;
    private String descrizione;
    private Long categoriaId;
    private String immagine;
    private LocalDateTime dataOraEvento;
    private LocalDateTime dataOraAperturaCancelli;
    private Integer postiDisponibili;
    private String luogo;
    private Long organizzatoreId;


}
