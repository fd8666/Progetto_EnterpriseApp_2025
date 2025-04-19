package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TipoPosto")
@Data
@NoArgsConstructor
public class TipoPosto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name="Nome")
    private String nome;

    @Column(name = "Prezzo")
    private Double prezzo;

    @Column(name = "PostiDisponibili")
    private int postiDisponibili;

}
