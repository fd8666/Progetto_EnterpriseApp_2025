package org.example.enterpriceappbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BigliettoEditSpettatoreDTO {

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

}
