package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "TagCategoria")
@Data
@NoArgsConstructor
public class TagCategoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", nullable = true)
    private String nome;

    @Column(name = "descrizione", nullable = true)
    private String descrizione;

    @OneToMany(mappedBy = "categoria",cascade = CascadeType.ALL)
    private List<Evento> eventi;

}
