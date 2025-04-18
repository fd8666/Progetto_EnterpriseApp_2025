package org.example.enterpriceappbackend.data.repository;

import org.example.enterpriceappbackend.data.entity.Features;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FeaturesRepository extends JpaRepository<Features, Long> , JpaSpecificationExecutor<Features> {

}
