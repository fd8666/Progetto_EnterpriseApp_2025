package org.example.enterpriceappbackend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class PagamentoRequestDTO {

    private String nomeTitolare;
    private String cognomeTitolare;
    private String numeroCarta;
    private String scadenza;
    private String cvv;
    private List<Long> bigliettiId;
}
