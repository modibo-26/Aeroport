package com.aeroport.notifications.client;

import com.aeroport.notifications.dto.ReservationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "SERVICE-RESERVATIONS")
public interface ReservationClient {
    @GetMapping("/reservations/vol/{volId}")
    List<ReservationDTO> getReservation(@PathVariable Long volId);
}
