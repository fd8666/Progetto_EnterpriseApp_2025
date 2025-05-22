package org.example.enterpriceappbackend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Utente;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseAuthentication {

    private String token;
    private String refreshToken;
    private Long utente;
}
