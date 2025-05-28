package it.unical.ea.eventra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import it.unical.ea.eventra.data.entity.Biglietto;
import it.unical.ea.eventra.data.constants.StatoPagamento;

import java.math.BigDecimal;
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
