package com.aeroport.notifications.dto;

import lombok.Data;

@Data
public class ReservationDTO {
    private Long id;

    private Long passagerId;

    private Long volId;
}
