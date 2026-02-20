package com.aeroport.reservations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementEvent {
    private Long paiementId;
    private Long reservationId;
    private Long passagerId;
    private String type;
    private String message;
    private String email;
}