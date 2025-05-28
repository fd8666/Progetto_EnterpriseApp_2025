package it.unical.ea.eventra.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @ManyToOne
    @JoinColumn(name = "wishlist_id",nullable = false)
    @JsonIgnore
    private Wishlist wishlist;

    @ManyToOne
    @JoinColumn(name = "utente_id",nullable = false)
    @JsonIgnore
    private Utente condivisaCon;

}
