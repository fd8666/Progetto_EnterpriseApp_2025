package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pagamenti")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_titolare", nullable = false)
    private String nomeTitolare;

    @Column(name = "cognome_titolare", nullable = false)
    private String cognomeTitolare;

    @Column(name = "numero_carta", nullable = false)
    private String numeroCarta;

    @Column(nullable = false)
    private String scadenza;

    @Column(nullable = false)
    private String cvv;

    @Column(nullable = false)
    private BigDecimal importo;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDateTime dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoPagamento stato;

    @ManyToOne
    @JoinColumn(name = "ordine_id", nullable = false)
    private Ordine ordine;

    @OneToMany(mappedBy = "pagamento", cascade = CascadeType.ALL)
    private List<Biglietto> biglietti;
}
