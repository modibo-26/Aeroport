package com.aeroport.vols.repository;

import com.aeroport.vols.entity.Vol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolRepository extends JpaRepository<Vol, Long> {
    List<Vol> findByDestination(String destination);
}
