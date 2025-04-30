package org.example.enterpriceappbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UtenteRegistrazioneDTO {
//
    @NotNull
    private String nome;
//
    @NotNull
    private String cognome;
//
    @Email
    private String credenzialiEmail;
//
    private String credenzialiPassword;
}
