package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name= "Zona")
@Getter @Setter
public class Zona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    @Size(max = 5000)
    private String descrizione;

    @Column(name = "totalePosti")
    private Integer totalePosti;

    @ManyToOne(optional = false)
    @JoinColumn(name="struttura_id", referencedColumnName = "id", nullable = false)
    private Struttura struttura;


}
