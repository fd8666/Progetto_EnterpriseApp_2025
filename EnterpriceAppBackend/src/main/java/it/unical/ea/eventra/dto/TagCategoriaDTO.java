package it.unical.ea.eventra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import it.unical.ea.eventra.data.entity.Evento;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagCategoriaDTO {

    private Long id;
    private String nome;
    private String descrizione;
    private List<Evento> eventi;

}
