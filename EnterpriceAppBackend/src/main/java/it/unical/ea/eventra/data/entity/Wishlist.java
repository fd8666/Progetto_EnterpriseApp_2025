package it.unical.ea.eventra.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import it.unical.ea.eventra.data.constants.Visibilita;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Wishlist") //nome tabella database
@Data
@NoArgsConstructor
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name="visibilità")
    @Enumerated(EnumType.STRING)
    private Visibilita visibilita;

    @Column(name = "dataCreazione")
    private LocalDateTime dataCreazione;

    @OneToOne //relazione con tabella Utente 1:1 viene unita tramite id
    @JoinColumn(name = "utente_id", referencedColumnName = "id", unique = true)
    private Utente utente;

    @ManyToMany //relazione N:N si crea una nuova tabella wl_ev con le chiavi esterne
    @JoinTable(
            name = "wl_ev",
            joinColumns = @JoinColumn(name = "wishlist_id"),
            inverseJoinColumns = @JoinColumn(name = "evento_id")
    )
    @JsonIgnore
    private List<Evento> eventi;


    @OneToMany(mappedBy = "wishlist",cascade = CascadeType.ALL)
    @JsonIgnore
    private List<WishlistCondivisa> condivisi;

}
