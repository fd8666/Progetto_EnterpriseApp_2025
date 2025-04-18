package org.example.enterpriceappbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Evento;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagCategoriaDto {

    private Long id;
    private String nome;
    private String descrizione;
    private List<Evento> eventi;


}
