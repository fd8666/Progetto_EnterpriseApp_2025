package org.example.enterpriceappbackend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class UtenteDTO {
    private Long id;

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(min = 2, max = 12, message = "Il nome deve essere tra 2 e 12 caratteri")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio")
    @Size(min = 2, max = 12, message = "Il cognome deve essere tra 2 e 12 caratteri")
    private String cognome;

    @NotBlank(message = "Il numero di telefono è obbligatorio")
    private String numerotelefono;

    @Email(message = "Inserire un indirizzo email valido")
    @NotBlank(message = "L'email è un campo obbligatorio")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "L'email deve contenere una '@' e un dominio valido")
    private String email;

    @NotNull(message = "La password è un campo obbligatorio")
    @Size(min = 6, message = "La password deve contenere almeno 6 caratteri")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", message = "La password deve contenere almeno un carattere minuscolo, uno maiuscolo, un numero e un carattere speciale")
    private String password;

    private String role;
}
