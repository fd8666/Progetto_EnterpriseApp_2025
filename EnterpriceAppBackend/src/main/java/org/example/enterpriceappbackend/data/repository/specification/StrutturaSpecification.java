package org.example.enterpriceappbackend.data.repository.specification;

import org.example.enterpriceappbackend.data.entity.Struttura;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class StrutturaSpecification {

    public static Specification<Struttura> withFilters(String nome, String categoria, String indirizzo) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Filtraggio per nome
            if (nome != null && !nome.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
            }
            // Filtraggio per categoria
            if (categoria != null && !categoria.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("categoria")), categoria.toLowerCase()));
            }
            // Filtraggio per indirizzo
            if (indirizzo != null && !indirizzo.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("indirizzo")), "%" + indirizzo.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
