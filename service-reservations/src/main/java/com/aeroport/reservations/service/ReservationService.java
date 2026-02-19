package com.aeroport.reservations.service;

import com.aeroport.reservations.client.VolClient;
import com.aeroport.reservations.dto.ReservationEvent;
import com.aeroport.reservations.entity.Reservation;
import com.aeroport.reservations.entity.Statut;
import com.aeroport.reservations.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService implements IReservationService {
    private final ReservationRepository repository;
    private final VolClient volClient;

    private final KafkaTemplate<String, ReservationEvent> kafka;

    @Override
    public Reservation addReservation(Reservation reservation) {
        int placesDisponible = volClient.getPlacesDisponibles(reservation.getVolId());

        if(placesDisponible < reservation.getNombrePlaces()) {
            throw new RuntimeException("Pas assez de places disponible");
        }

        reservation.setStatut(Statut.EN_ATTENTE);
        reservation.setDateReservation(LocalDateTime.now());

        Reservation saved = repository.save(reservation);

        volClient.removePlaces(reservation.getVolId(), reservation.getNombrePlaces());

        kafka.send("reservation-events", new ReservationEvent(saved.getId(), saved.getVolId(), saved.getPassagerId(), "CRÉATION", "PASSAGER", "Réservation créé"));

        return saved;
    }

    @Override
    public Reservation findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Reservation non trouvée"));
    }

    @Override
    public Reservation annulerReservation(Long id, String source) {
        Reservation reservation = findById(id);
        reservation.setStatut(Statut.ANNULEE);
        Reservation saved = repository.save(reservation);

        volClient.addPlaces(reservation.getVolId(), reservation.getNombrePlaces());

        kafka.send("reservation-events", new ReservationEvent(saved.getId(), saved.getVolId(), saved.getPassagerId(), "ANNULATION", source, "Réservation annulée"));

        return saved;
    }

    @Override
    public List<Reservation> findByPassager(Long passagerId) {
        return repository.findByPassagerId(passagerId);
    }

    @Override
    public Reservation confirmerReservation(Long id) {
        Reservation reservation =  findById(id);
        reservation.setStatut(Statut.CONFIRMEE);
        Reservation saved = repository.save(reservation);

        kafka.send("reservation-events", new ReservationEvent(saved.getId(), saved.getVolId(), saved.getPassagerId(), "CONFIRMATION", "PASSAGER", "Réservation confirmée"));

        return saved;
    }

    @Override
    public List<Reservation> findByVol(Long volId) {
        return repository.findByVolId(volId);
    }
}
