package com.aeroport.paiement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Paiements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {

        @Id
        @Column(nullable = false, unique = true)
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private Long reservationId;

        @Column(nullable = false)
        private Long passagerId;

        @Column(nullable = false)
        private BigDecimal montant;

        private String email;

        private String stripeSessionId;

        private String stripePaymentIntentId;

        @Enumerated(EnumType.STRING)
        private StatutPaiement statut;

        private LocalDateTime createdAt;

        @PrePersist
        protected void onCreate() {
                createdAt = LocalDateTime.now();
        }
}