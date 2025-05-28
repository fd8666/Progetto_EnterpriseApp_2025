package it.unical.ea.eventra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import it.unical.ea.eventra.data.entity.Pagamento;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class OrdineDTO {

    private Long id;
    private Pagamento pagamento;
    private LocalDateTime dataCreazione;
    private String emailProprietario;
    private Double prezzoTotale;
    private Long proprietarioId;
}
