package com.aeroport.reservations.repository;

import com.aeroport.reservations.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByPassagerId(Long passagerId);
    List<Reservation> findByVolId(Long volId);
}
