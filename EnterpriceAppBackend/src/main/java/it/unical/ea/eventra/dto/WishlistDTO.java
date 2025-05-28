package it.unical.ea.eventra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import it.unical.ea.eventra.data.constants.Visibilita;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO {

    private Long id;
    private Long utenteId;
    private Visibilita visibilita;
    private LocalDateTime dataCreazione;
    private List<Long> eventi;

}
