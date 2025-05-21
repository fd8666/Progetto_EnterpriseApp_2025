package org.example.enterpriceappbackend.data.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "Biglietto")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE BIGLIETTO SET deleted = 1 WHERE id=?")
@SQLRestriction("deleted = 0")
public class Biglietto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome_spettatore", nullable = false)
    private String nomeSpettatore;

    @Column(name = "cognome_spettatore", nullable = false)
    private String cognomeSpettatore;

    @Column(name = "email_spettatore", nullable = false)
    private String emailSpettatore;

    @Column(name = "deleted", nullable = false)
    private Integer deleted;

    @ManyToOne
    @JoinColumn(name = "evento_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private Evento evento;

    @ManyToOne
    @JoinColumn(name = "tipo_posto_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private TipoPosto tipoPosto;


    @ManyToOne
    @JoinColumn(name = "pagamento_id", referencedColumnName = "id")//opzionale in caso di pagamento non ancora effettuato
    @JsonIgnore
    @JsonBackReference
    private Pagamento pagamento;

    @CreatedDate
    @Column(name = "data_creazione", updatable = false)
    private LocalDateTime dataCreazione;

}
