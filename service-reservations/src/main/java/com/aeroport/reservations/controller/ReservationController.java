package com.aeroport.reservations.controller;

import com.aeroport.reservations.entity.Reservation;
import com.aeroport.reservations.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @PostMapping()
    public Reservation addReservation(@RequestBody Reservation reservation) {
        return service.addReservation(reservation);
    }

    @GetMapping("/{id}")
    public Reservation findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}/annuler")
    public Reservation annulerReservation(@PathVariable Long id){
        return service.annulerReservation(id, "PASSAGER");
    }

    @GetMapping("/passager/{passagerId}")
    public List<Reservation> findByPassager(@PathVariable Long passagerId) {
        return service.findByPassager(passagerId);
    }

    @PutMapping("/{id}/confirmer")
    public Reservation confirmerReservation(@PathVariable Long id) {
        return service.confirmerReservation(id);
    }

    @GetMapping("/vol/{volId}")
    public List<Reservation> findByVol(@PathVariable Long volId) {
        return service.findByVol(volId);
    }

}

