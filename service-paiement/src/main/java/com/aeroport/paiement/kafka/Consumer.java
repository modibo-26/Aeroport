package com.aeroport.paiement.kafka;

import com.aeroport.paiement.dto.ReservationEvent;
import com.aeroport.paiement.service.PaiementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Consumer {

    private final PaiementService service;

    @KafkaListener(topics = "reservation-events")
    public void consumeReservation(String message) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ReservationEvent event = mapper.readValue(message, ReservationEvent.class);

        if ("ANNULATION".equals(event.getType())) {
            service.rembourserParReservation(event.getReservationId());
        }
    }
}