package org.example.enterpriceappbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class UtenteDTO {
    private long id;
    private String email;
    private String password;
    private String nome;
    private String cognome;
    private String role;
}
