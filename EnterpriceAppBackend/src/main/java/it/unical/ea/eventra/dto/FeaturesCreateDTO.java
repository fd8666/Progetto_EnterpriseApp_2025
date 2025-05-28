package it.unical.ea.eventra.dto;

import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class FeaturesCreateDTO {

    private Long id;

    @NotBlank(message = "La zona non può essere vuota")
    @Size(min = 2, max = 100, message = "La zona deve contenere tra 3 e 100 caratteri")
    @Pattern(regexp = "[A-Za-zÀ-ÖØ-öø-ÿ]+(?: [A-Za-zÀ-ÖØ-öø-ÿ]+)*$", message = "La zona può contenere solo lettere e spazi")
    private String zone;

    @NotBlank(message = "Le features non possono essere vuote")
    @Size(min = 5, max = 255, message = "Le features devono contenere tra 5 e 255 caratteri")
    private String features;

    @NotNull(message = "L'ID del tipo di posto è obbligatorio")
    @Positive(message = "L'ID del tipo di posto deve essere un numero positivo")
    private Long tipoPostoId;
}
