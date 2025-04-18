package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "biglietti")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Biglietto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "nome_spettatore", nullable = false)
    private String nomeSpettatore;

    @Column(name = "cognome_spettatore", nullable = false)
    private String cognomeSpettatore;

    @Column(name = "email_spettatore")
    private String emailSpettatore;

    @Column(name = "data_creazione", nullable = false)
    private LocalDateTime dataCreazione;

    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    /*
    @ManyToOne
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;
    */

    @ManyToOne
    @JoinColumn(name = "pagamento_id", nullable = false)
    private Pagamento pagamento;
}
