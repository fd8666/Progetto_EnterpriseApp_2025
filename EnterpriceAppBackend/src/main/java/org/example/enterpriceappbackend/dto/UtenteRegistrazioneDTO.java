package org.example.enterpriceappbackend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UtenteRegistrazioneDTO {

    @NotNull(message = "Il nome è un campo obbligatorio")
    @Size(min = 2, max = 15, message = "Il nome deve essere tra 2 e 15 caratteri")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ]+(?: [A-Za-zÀ-ÖØ-öø-ÿ]+)*$", message = "Il nome deve contenere solo lettere e spazi")
    private String nome;

    @NotNull(message = "Il cognome è un campo obbligatorio")
    @Size(min = 2, max = 15, message = "Il cognome deve essere tra 2 e 15 caratteri")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ]+(?: [A-Za-zÀ-ÖØ-öø-ÿ]+)*$", message = "Il cognome deve contenere solo lettere e spazi")
    private String cognome;

    @Email(message = "Inserire un indirizzo email valido")
    @NotBlank(message = "L'email è un campo obbligatorio")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "L'email deve contenere una '@' e un dominio valido")
    private String credenzialiEmail;

    @NotNull(message = "La password è un campo obbligatorio")
    @Size(min = 6, message = "La password deve contenere almeno 6 caratteri")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", message = "La password deve contenere almeno un carattere minuscolo, uno maiuscolo, un numero e un carattere speciale")
    private String credenzialiPassword;
}
