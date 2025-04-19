package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

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

    @ManyToOne //crea una tabella con la relazione ManyToOne con Utente(Organizzatore)
    @JoinColumn(name = "organizzatore_id", referencedColumnName = "id", nullable = false)
    private Utente organizzatore;

    @ManyToOne //crea una tabella con la relazione ManyToOne con TagCategoriaDTO
    @JoinColumn(name = "categoria_id", referencedColumnName = "id", nullable = false)
    private TagCategoria categoria;

    @OneToMany(mappedBy = "evento",cascade = CascadeType.ALL)
    private List<Biglietto> biglietti;

    @ManyToMany //relazione N:N con struttura join
    @JoinTable(
            name = "ev_st",
            joinColumns = @JoinColumn(name = "evento_id"),
            inverseJoinColumns = @JoinColumn(name = "struttura_id")
    )
    private List<Struttura> strutture;
}
