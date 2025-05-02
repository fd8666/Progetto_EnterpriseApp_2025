package org.example.enterpriceappbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterpriceappbackend.data.constants.StatoPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class PagamentoResponseDTO {

    private Long id;
    private BigDecimal importo;
    private LocalDateTime dataPagamento;
    private StatoPagamento stato;
    private String numeroOrdine;
    private List<BigliettoInfoDTO> biglietti;
    private String messaggio;
}
