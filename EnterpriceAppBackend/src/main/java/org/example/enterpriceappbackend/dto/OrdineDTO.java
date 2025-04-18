package org.example.enterpriceappbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdineDTO {
    private Long id;
    private List<PagamentoDTO> pagamenti;
    private LocalDateTime dataCreazione;
    private String emailProprietario;
    private Double prezzoTotale;
    private Long proprietarioId;
}
