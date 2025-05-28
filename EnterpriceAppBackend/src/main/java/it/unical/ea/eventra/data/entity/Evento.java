package it.unical.ea.eventra.data.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Evento")
@Data
@NoArgsConstructor
@SQLDelete(sql = "UPDATE EVENTO SET deleted = 1 WHERE id=?")
@SQLRestriction("deleted = 0")
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

    @Column(name = "deleted", nullable = false)
    private Integer deleted;

    @ManyToOne //crea una tabella con la relazione ManyToOne con Utente(Organizzatore)
    @JoinColumn(name = "organizzatore_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private Utente organizzatore;

    @ManyToOne //crea una tabella con la relazione ManyToOne con TagCategoriaDTO
    @JoinColumn(name = "categoria_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    private TagCategoria categoria;

    @OneToMany(mappedBy = "evento",cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Biglietto> biglietti;

    @ManyToOne
    @JoinColumn(name = "struttura_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private Struttura struttura;

    @OneToMany(mappedBy = "evento",cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TipoPosto> tipiPosto;


}
