package com.aeroport.reservations.service;

import com.aeroport.reservations.entity.Reservation;

import java.util.List;

public interface IReservationService {
    Reservation addReservation(Reservation reservation);
    Reservation findById(Long id);
    Reservation annulerReservation(Long id, String source);
    List<Reservation> findByPassager(Long passagerId);
    Reservation confirmerReservation(Long id);
    List<Reservation> findByVol(Long volId);
}
