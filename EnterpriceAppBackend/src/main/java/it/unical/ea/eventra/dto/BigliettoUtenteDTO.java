package it.unical.ea.eventra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BigliettoUtenteDTO {
    private Long utenteId;
    private List<BigliettoInfoDTO> biglietti;
}