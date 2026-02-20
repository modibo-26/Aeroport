package com.aeroport.paiement.controller;

import com.aeroport.paiement.config.StripeConfig;
import com.aeroport.paiement.entity.Paiement;
import com.aeroport.paiement.entity.StatutPaiement;
import com.aeroport.paiement.repository.PaiementRepository;
import com.aeroport.paiement.service.PaiementService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService service;
    private final PaiementRepository repository;
    private final StripeConfig stripeConfig;

    @PostMapping
    public ResponseEntity<String> creerSession(
            @RequestHeader("X-User-Email")  String email,
            @RequestParam Long reservationId,
            @RequestParam Long passagerId,
            @RequestParam BigDecimal montant) throws StripeException {
        String url = service.creerSessionPaiement(email, reservationId, passagerId, montant);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Signature invalide");
        }

        try {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = deserializer.getObject().isPresent()
                    ? deserializer.getObject().get()
                    : deserializer.deserializeUnsafe();

            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) stripeObject;
                service.confirmerPaiement(session.getId());
            }

            if ("checkout.session.expired".equals(event.getType())) {
                Session session = (Session) stripeObject;
                repository.findByStripeSessionId(session.getId()).ifPresent(paiement -> {
                    paiement.setStatut(StatutPaiement.ECHOUEE);
                    repository.save(paiement);
                });
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur webhook: " + e.getMessage());
        }

        return ResponseEntity.ok("OK");
    }

    @PostMapping("/{id}/rembourser")
    public ResponseEntity<Paiement> rembourser(@PathVariable Long id) throws StripeException {
        return ResponseEntity.ok(service.rembourser(id));
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<Paiement> findByReservation(@PathVariable Long reservationId) {
        return repository.findByReservationId(reservationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Paiement> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}