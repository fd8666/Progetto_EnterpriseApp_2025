package org.example.enterpriceappbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Evento;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDto {

    private Long id;
    private Long proprietarioId;
    private Boolean visibilita;
    private LocalDate dataCreazione;
    private List<Evento> eventi;

}
