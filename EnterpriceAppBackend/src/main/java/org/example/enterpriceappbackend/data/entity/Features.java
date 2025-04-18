package org.example.enterpriceappbackend.data.entity;


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
    @Column(name = "ID")
    private Long id;

    @Column(name = "Zona")
    private String zona;

    @Column(name = "Features")
    private String features;

}
