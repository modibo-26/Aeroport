package com.aeroport.reservations.kafka;

import com.aeroport.reservations.dto.PaiementEvent;
import com.aeroport.reservations.dto.VolEvent;
import com.aeroport.reservations.entity.Reservation;
import com.aeroport.reservations.repository.ReservationRepository;
import com.aeroport.reservations.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Consumer {

    private final ReservationService service;
    private final ReservationRepository repository;

    @KafkaListener(topics = "paiement-events")
    public void consumePaiement(String message) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PaiementEvent event = mapper.readValue(message, PaiementEvent.class);

        repository.findById(event.getReservationId()).ifPresent(reservation -> {
            if ("PAIEMENT".equals(event.getType())) {
                service.confirmerReservation(reservation.getId());
            }
            if ("REMBOURSEMENT".equals(event.getType())) {
                service.annulerReservation(reservation.getId(), "PASSAGER");
            }
        });
    }

    @KafkaListener(topics = "vol-events")
    public void consumeVol(String message) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        VolEvent event = mapper.readValue(message, VolEvent.class);

        if ("ANNULE".equals(event.getType())) {
            List<Reservation> reservations = repository.findByVolId(event.getVolId());
            for (Reservation reservation : reservations) {
                service.annulerReservation(reservation.getId(), "VOL");
            }
        }
    }
}