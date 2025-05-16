package org.example.enterpriceappbackend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TipoPostoDTO {
    private Long id;
    @NotBlank
    private String nome;

    @DecimalMin(value = "0.0", inclusive = false)
    private double prezzo;

    @Min(0)
    private int postiDisponibili;

    @NotNull
    private Long eventoId;

    @NotNull
    private Long featuresId;
}