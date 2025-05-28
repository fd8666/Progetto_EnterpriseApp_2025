package it.unical.ea.eventra.data.repository.specification;

import it.unical.ea.eventra.data.entity.Struttura;
import org.springframework.data.jpa.domain.Specification;

public class StrutturaSpecification {

    public static Specification<Struttura> hasNome(String nome) {
        return (root, query, builder) -> nome == null ? null :
                builder.like(builder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    public static Specification<Struttura> hasCategoria(String categoria) {
        return (root, query, builder) -> categoria == null ? null :
                builder.equal(root.get("categoria"), categoria);
    }

    public static Specification<Struttura> hasIndirizzo(String indirizzo) {
        return (root, query, builder) -> indirizzo == null ? null :
                builder.like(builder.lower(root.get("indirizzo")), "%" + indirizzo.toLowerCase() + "%");
    }
}
