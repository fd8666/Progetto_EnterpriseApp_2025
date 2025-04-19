package org.example.enterpriceappbackend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BigliettoCreateDTO {//per la creazione dei vari biglietti
    @NotBlank(message = "Il nome dello spettatore è campo obbligatorio!")
    @Size(min = 3, max = 30)
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ]+(?: [A-Za-zÀ-ÖØ-öø-ÿ]+)*$", message = "Il nome deve contenere solo lettere e spazi.")
    private String nomeSpettatore;

    @NotBlank(message = "Il cognome dello spettatore è campo obbligatorio!")
    @Size(min = 3, max = 30)
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ]+(?: [A-Za-zÀ-ÖØ-öø-ÿ]+)*$", message = "Il cognome deve contenere solo lettere e spazi.")
    private String cognomeSpettatore;

    @Email
    private String emailSpettatore;

    @NotNull
    private Long eventoId;

    @NotNull
    private Long postoId;
}
