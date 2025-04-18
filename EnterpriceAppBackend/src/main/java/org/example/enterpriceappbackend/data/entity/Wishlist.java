package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Wishlist")
@Data
@NoArgsConstructor
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name="visibilità")
    @Enumerated(EnumType.STRING)
    private Visibilita visibilita;

    @Column(name = "dataCreazione")
    private LocalDateTime dataCreazione;

    @OneToOne
    @JoinColumn(name = "proprietario_id", nullable = false, unique = true)
    private Utente proprietario;

    @ManyToMany
    @JoinTable(
            name = "wl_ev",
            joinColumns = @JoinColumn(name = "wishlist_id"),
            inverseJoinColumns = @JoinColumn(name = "evento_id")
    )
    private List<Evento> eventi;





}
