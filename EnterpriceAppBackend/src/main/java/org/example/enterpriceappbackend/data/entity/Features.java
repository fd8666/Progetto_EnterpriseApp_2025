package org.example.enterpriceappbackend.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Features")
@Data
@NoArgsConstructor
public class Features {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Zona")
    private String zona;

    @Column(name = "Features")
    private String features;;

    @OneToOne
    @JoinColumn(name = "tipo_posto_id", nullable = false)
    @JsonIgnore
    private TipoPosto tipoPosto;

}
