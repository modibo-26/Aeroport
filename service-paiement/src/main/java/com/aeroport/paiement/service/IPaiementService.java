package com.aeroport.paiement.service;

import com.aeroport.paiement.entity.Paiement;

import com.stripe.exception.StripeException;

import java.math.BigDecimal;

public interface IPaiementService {
    String creerSessionPaiement(Long reservationId, Long passagerId, BigDecimal montant) throws StripeException;
    Paiement confirmerPaiement(String stripeSessionId) throws StripeException;
    Paiement rembourser(Long paiementId) throws StripeException;
    Paiement rembourserParReservation(Long reservationId) throws StripeException;
    Paiement findByReservationId(Long reservationId);
    Paiement findById(Long id);
}
