package org.example.enterpriceappbackend.dto;

import java.util.List;

public class TipoPostoDTO {
    private Long id;
    private String nome;
    private double prezzo;
    private int postiDisponibili;
    private String descrizione;
    private Long eventoId;
    private List<Long> featuresId;
}
