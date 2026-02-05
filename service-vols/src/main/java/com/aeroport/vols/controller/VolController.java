package com.aeroport.vols.controller;

import com.aeroport.vols.entity.Statut;
import com.aeroport.vols.entity.Vol;
import com.aeroport.vols.service.VolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vols")
public class VolController {
    private final VolService service;

    @PostMapping()
    public Vol addVol(@RequestBody Vol vol) {
        return service.AddVol(vol);
    }

    @GetMapping()
    public List<Vol> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Vol findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public Vol editVol(@PathVariable Long id, @RequestBody Vol vol) {
        return service.editVol(id, vol);
    }

    @DeleteMapping("/{id}")
    public void deleteVol(@PathVariable Long id) {
        service.deleteVol(id);
    }

    @GetMapping("/{id}/places-disponibles")
    public int placeDisponible(@PathVariable Long id) {
        return service.placeDisponible(id);
    }

    @PutMapping("/{id}/statut")
    public Vol updateStatut(@PathVariable Long id, @RequestBody Statut statut) {
        return service.updateStatut(id, statut);
    }

    @GetMapping("/destination/{destination}")
    public List<Vol> getByDestination(@PathVariable String destination) {
        return service.findByDestination(destination);
    }

    @PutMapping("/{id}/add/{places}")
    public Vol addPlaces(@PathVariable Long id, @PathVariable int places) {
        return service.updatePlaces(id, places);
    }

    @PutMapping("/{id}/remove/{places}")
    public Vol removePlaces(@PathVariable Long id, @PathVariable int places) {
        return service.updatePlaces(id, -places);
    }




}
