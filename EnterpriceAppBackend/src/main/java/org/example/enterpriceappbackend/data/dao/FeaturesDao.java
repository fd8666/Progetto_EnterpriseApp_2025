package org.example.enterpriceappbackend.data.dao;

import org.example.enterpriceappbackend.data.entity.Features;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FeaturesDao extends JpaRepository<Features, Long> , JpaSpecificationExecutor<Features> {

}
