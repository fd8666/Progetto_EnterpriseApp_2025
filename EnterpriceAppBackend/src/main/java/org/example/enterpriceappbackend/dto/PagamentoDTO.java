package org.example.enterpriceappbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Biglietto;
import org.example.enterpriceappbackend.data.constants.StatoPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoDTO {
    private Long id;
    private String nomeTitolare;
    private String cognomeTitolare;
    private String numeroCarta;
    private LocalDateTime scadenza;
    private String cvv;
    private BigDecimal importo;
    private LocalDateTime dataPagamento;
    private StatoPagamento stato;
    private Long ordineId;
    private List<Biglietto> biglietti;
}
