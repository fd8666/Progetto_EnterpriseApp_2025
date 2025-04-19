package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "WhislistCondivisa")
@Data
@NoArgsConstructor
public class WishlistCondivisa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

}
