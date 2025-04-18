package org.example.enterpriceappbackend.data.repository;

import org.example.enterpriceappbackend.data.entity.WishlistCondivisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WishlistCondivisaRepository extends JpaRepository<WishlistCondivisa, Long> , JpaSpecificationExecutor<WishlistCondivisa> {
}
