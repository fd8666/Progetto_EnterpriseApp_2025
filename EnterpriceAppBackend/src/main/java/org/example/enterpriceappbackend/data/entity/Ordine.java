package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ordini")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ordine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_creazione", nullable = false)
    private LocalDateTime dataCreazione;

    @Column(name = "email_proprietario", nullable = false)
    private String emailProprietario;

    @Column(name = "prezzo_Totale", nullable = false)
    private double prezzoTotale;

    @ManyToOne
    @JoinColumn(name = "proprietario_id")
    private Utente proprietario;

    @OneToMany(mappedBy = "ordine", cascade = CascadeType.ALL)
    private List<Pagamento> pagamenti;
}
