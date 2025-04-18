package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Evento")
@Data
@NoArgsConstructor
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", nullable = true)
    private String nome;

    @Column(name = "descrizione", nullable = true)
    private String descrizione;

    @ManyToOne(fetch = FetchType.LAZY) //crea una tabella con la relazione ManyToOne con TagCategoriaDto
    @JoinColumn(name = "categoria_id", nullable = false)
    private TagCategoria categoria;

    @Column(name = "immagine", nullable = true)
    private String immagine;

    @Column(name = "data_ora_evento", nullable = false)
    private LocalDateTime dataOraEvento;

    @Column(name = "data_ora_apertura_cancelli", nullable = false)
    private LocalDateTime dataOraAperturaCancelli;

    @Column(name = "posti_disponibili", nullable = false)
    private Integer postiDisponibili;

    @Column(name = "luogo", nullable = false)
    private String luogo;

    @ManyToOne(fetch = FetchType.LAZY) //crea una tabella con la relazione ManyToOne con Utente(Organizzatore)
    @JoinColumn(name = "organizzatore_id", nullable = false)
    private Utente organizzatore;
}
