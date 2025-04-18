package org.example.enterpriceappbackend.data.dao;

import org.example.enterpriceappbackend.data.entity.WishlistCondivisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WishlistCondivisaDao extends JpaRepository<WishlistCondivisa, Long> , JpaSpecificationExecutor<WishlistCondivisa> {
}
