package com.aeroport.paiement.service;

import com.aeroport.paiement.config.StripeConfig;
import com.aeroport.paiement.entity.Paiement;
import com.aeroport.paiement.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.aeroport.paiement.dto.PaiementEvent;
import com.aeroport.paiement.entity.StatutPaiement;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaiementService implements IPaiementService {

    private final PaiementRepository repository;
    private final StripeConfig stripe;
    private final KafkaTemplate<String, PaiementEvent> kafka;

    @Override
    public String creerSessionPaiement(String email, Long reservationId, Long passagerId, BigDecimal montant) throws StripeException {
        repository.findByReservationId(reservationId).ifPresent(existing -> {
            if (existing.getStatut() == StatutPaiement.PAYEE) {
                throw new RuntimeException("Réservation déjà payée #" + reservationId);
            }
            repository.delete(existing);
        });

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripe.getSuccessUrl())
                .setCancelUrl(stripe.getCancelUrl())
                .setCustomerEmail(email)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(montant.multiply(BigDecimal.valueOf(100)).longValue())
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Réservation #" + reservationId)
                                        .build())
                                .build())
                        .build())
                .build();

        Session session = Session.create(params);

        Paiement paiement = Paiement.builder()
                .reservationId(reservationId)
                .passagerId(passagerId)
                .montant(montant)
                .stripeSessionId(session.getId())
                .statut(StatutPaiement.EN_ATTENTE)
                .build();

        repository.save(paiement);

        return session.getUrl();
    }

    @Override
    public Paiement confirmerPaiement(String stripeSessionId) throws StripeException {
        Paiement paiement = repository.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() -> new RuntimeException("Paiement introuvable pour session: " + stripeSessionId));
        Session session = Session.retrieve(stripeSessionId);
        paiement.setStripePaymentIntentId(session.getPaymentIntent());
        paiement.setStatut(StatutPaiement.PAYEE);
        Paiement saved = repository.save(paiement);

        kafka.send("paiement-events", new PaiementEvent(saved.getId(), saved.getReservationId(), saved.getPassagerId(), "PAIEMENT", "Paiement confirmé"));

        return saved;
    }

    @Override
    public Paiement rembourser(Long paiementId) throws StripeException {
        Paiement paiement = repository.findById(paiementId).orElseThrow();
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paiement.getStripePaymentIntentId())
                .build();
        Refund.create(params);
        paiement.setStatut(StatutPaiement.REMBOURSEE);
        Paiement saved = repository.save(paiement);

        kafka.send("paiement-events", new PaiementEvent(saved.getId(), saved.getReservationId(), saved.getPassagerId(), "REMBOURSEMENT", "Paiement remboursé"));

        return saved;
    }

    @Override
    public Paiement rembourserParReservation(Long reservationId) throws StripeException {
        Paiement paiement = repository.findByReservationId(reservationId).orElse(null);
        if (paiement == null) return null;
        if (paiement.getStatut() == StatutPaiement.REMBOURSEE || paiement.getStatut() == StatutPaiement.ECHOUEE) {
            return paiement;
        }
        if (paiement.getStatut() == StatutPaiement.EN_ATTENTE) {
            paiement.setStatut(StatutPaiement.ECHOUEE);
            return repository.save(paiement);
        }
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paiement.getStripePaymentIntentId())
                .build();
        Refund.create(params);
        paiement.setStatut(StatutPaiement.REMBOURSEE);
        return repository.save(paiement);
    }

    @Override
    public Paiement findByReservationId(Long reservationId) {
        return repository.findByReservationId(reservationId).orElseThrow();
    }

    @Override
    public Paiement findById(Long id) {
        return repository.findById(id).orElseThrow();
    }
}
