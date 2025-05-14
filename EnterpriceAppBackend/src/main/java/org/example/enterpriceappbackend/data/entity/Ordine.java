package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "Ordine")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ordine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @CreatedDate
    @Column(name = "data_creazione", updatable = false)
    private LocalDateTime dataCreazione;

    @Column(name = "email_proprietario", nullable = false)
    private String emailProprietario;

    @Column(name = "prezzo_Totale", nullable = false)
    private double prezzoTotale;

    @ManyToOne
    @JoinColumn(name = "proprietario_id")
    @JsonIgnore
    private Utente proprietario;

    @OneToOne(mappedBy = "ordine", cascade = CascadeType.ALL)
    private Pagamento pagamento;
}
