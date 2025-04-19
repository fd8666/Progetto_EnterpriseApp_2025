package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "Biglietto")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Biglietto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome_spettatore", nullable = false)
    private String nomeSpettatore;

    @Column(name = "cognome_spettatore", nullable = false)
    private String cognomeSpettatore;

    @Column(name = "email_spettatore")
    private String emailSpettatore;

    @ManyToOne
    @JoinColumn(name = "evento_id", referencedColumnName = "id", nullable = false)
    private Evento evento;

    @ManyToOne
    @JoinColumn(name = "tipo_posto_id", referencedColumnName = "id", nullable = false)
    private TipoPosto tipoPosto;

    @ManyToOne
    @JoinColumn(name = "pagamento_id", referencedColumnName = "id")//opzionale in caso di pagamento non ancora effettuato
    private Pagamento pagamento;

    @CreatedDate
    @Column(name = "data_creazione", updatable = false)
    private LocalDateTime dataCreazione;

}
