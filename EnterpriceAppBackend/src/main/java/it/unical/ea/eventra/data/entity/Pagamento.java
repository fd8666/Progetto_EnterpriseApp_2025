package it.unical.ea.eventra.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import it.unical.ea.eventra.data.constants.StatoPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Pagamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome_titolare", nullable = false)
    private String nomeTitolare;

    @Column(name = "cognome_titolare", nullable = false)
    private String cognomeTitolare;

    @Column(name = "numero_carta", nullable = false)
    private String numeroCarta;

    @Column(name = "scadenza", nullable = false)
    private LocalDateTime scadenza;

    @Column(name = "cvv", nullable = false)
    private String cvv;

    @Column(name = "importo", nullable = false)
    private BigDecimal importo;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDateTime dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoPagamento stato;

    @OneToOne
    @JoinColumn(name = "ordine_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private Ordine ordine;

    @OneToMany(mappedBy = "pagamento", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Biglietto> biglietti;
}

