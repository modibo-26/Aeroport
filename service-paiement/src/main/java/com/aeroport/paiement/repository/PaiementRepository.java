package com.aeroport.paiement.repository;

import com.aeroport.paiement.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    Optional<Paiement> findByStripeSessionId(String stripeSessionId);
    Optional<Paiement> findByReservationId(Long reservationId);
}