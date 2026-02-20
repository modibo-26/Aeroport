package com.aeroport.notifications.kafka;

import com.aeroport.notifications.client.ReservationClient;
import com.aeroport.notifications.dto.PaiementEvent;
import com.aeroport.notifications.dto.ReservationDTO;
import com.aeroport.notifications.dto.ReservationEvent;
import com.aeroport.notifications.dto.VolEvent;
import com.aeroport.notifications.entity.Notification;
import com.aeroport.notifications.service.EmailService;
import com.aeroport.notifications.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Consumer {

    private final NotificationService service;

    private final ReservationClient client;

    private final EmailService emailService;

    @KafkaListener(topics = "reservation-events")
    public void consumeReservation(String message) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        ReservationEvent event = mapper.readValue(message, ReservationEvent.class);

        if ("ANNULATION".equals(event.getType()) && "VOL".equals(event.getSource())) {
            return;
        }

        Notification notification = Notification.builder()
                .passagerId(event.getPassagerId())
                .volId(event.getVolId())
                .reservationId(event.getReservationId())
                .message(event.getMessage())
                .dateCreation(LocalDateTime.now())
                .lue(false)
                .build();
        service.addNotification(notification);
    }

    @KafkaListener(topics = "paiement-events")
    public void consumePaiement(String message) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PaiementEvent event = mapper.readValue(message, PaiementEvent.class);

        if (event.getEmail() == null) return;

        if ("PAIEMENT".equals(event.getType())) {
            emailService.envoyerEmail(event.getEmail(),
                    "Confirmation de paiement - Réservation #" + event.getReservationId(),
                    "Votre paiement a été confirmé pour la réservation #" + event.getReservationId() + ".\nMerci pour votre achat !");
        } else if ("REMBOURSEMENT".equals(event.getType())) {
            emailService.envoyerEmail(event.getEmail(),
                    "Remboursement effectué - Réservation #" + event.getReservationId(),
                    "Votre remboursement a été effectué pour la réservation #" + event.getReservationId() + ".\nLe montant sera crédité sous quelques jours.");
        }
    }

    @KafkaListener(topics = "vol-events")
    public void consumeVol(String message) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        VolEvent event = mapper.readValue(message, VolEvent.class);
        List<ReservationDTO> reservations = client.getReservation(event.getVolId());
        Set<Long> passagerIds = reservations.stream()
                .map(ReservationDTO::getPassagerId)
                .collect(Collectors.toSet());

        for (Long passengerId : passagerIds) {
            Notification notification = Notification.builder()
                    .passagerId(passengerId)
                    .volId(event.getVolId())
                    .message(event.getMessage())
                    .dateCreation(LocalDateTime.now())
                    .lue(false)
                    .build();
            service.addNotification(notification);
        }

    }


}
