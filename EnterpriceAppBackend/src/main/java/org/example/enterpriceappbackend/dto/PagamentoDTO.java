package org.example.enterpriceappbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Biglietto;
import org.example.enterpriceappbackend.data.constants.StatoPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoDTO {
    private Long id;
    private String nomeTitolare;
    private String cognomeTitolare;
    private String numeroCarta;
    private String scadenza;
    private String cvv;
    private BigDecimal importo;
    private LocalDate dataPagamento;
    private StatoPagamento stato;
    private Long ordineId;
    private List<BigliettoInfoDTO> bigliettiInfo;
    private List<Biglietto> biglietti;
}
