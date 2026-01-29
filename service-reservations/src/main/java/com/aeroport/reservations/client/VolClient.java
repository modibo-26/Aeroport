package com.aeroport.reservations.client;

import com.aeroport.reservations.dto.VolDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "SERVICE-VOLS")
public interface VolClient {
    @GetMapping("/vols/{id}")
    VolDTO getVol (@PathVariable Long id);

    @GetMapping("/vols/{id}/places-disponibles")
    int getPlacesDisponibles(@PathVariable Long id);

}
