package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name= "Struttura")
@Getter @Setter
public class Struttura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "immagine")//piantina zone
    private String immagine; // ad esempio, URL o path al file

    @Column(name = "indirizzo")
    private String indirizzo;

    @Column(name = "coordinate_lat")
    private String coordinateLatitude;

    @Column(name = "coordinate_long")
    private String coordinateLongitude;

    @OneToMany(mappedBy = "struttura", cascade = CascadeType.ALL,fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Zona> zone;

    @ManyToMany(mappedBy = "strutture")
    private List<Evento> eventi;


}
