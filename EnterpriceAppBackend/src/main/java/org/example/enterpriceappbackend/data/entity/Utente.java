package org.example.enterpriceappbackend.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterpriceappbackend.data.constants.Role;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Utente")
public class Utente{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "cognome", nullable = false)
    private String cognome;

    @Column(name = "numero_telefono")
    private String numeroTelefono;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "organizzatore",cascade = CascadeType.ALL)
    private List<Evento> eventi;

    @OneToOne(mappedBy = "utente",cascade = CascadeType.ALL)
    private Wishlist wishlist;
}
