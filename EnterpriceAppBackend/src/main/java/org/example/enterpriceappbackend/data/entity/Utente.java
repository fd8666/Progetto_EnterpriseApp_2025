package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "Utente")
@Data
@NoArgsConstructor
public class Utente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;




    @OneToOne(mappedBy = "proprietario")
    private Wishlist wishlist;





}
