package com.aeroport.vols.service;

import com.aeroport.vols.entity.Statut;
import com.aeroport.vols.entity.Vol;

import java.util.List;

public interface IVolService {
    Vol AddVol(Vol vol);
    List<Vol> findAll();
    Vol findById(Long id);
    Vol editVol(Long id, Vol vol);
    void deleteVol(Long id);
    int placeDisponible(Long id);
    Vol updateStatut(Long id, Statut statut);
    List<Vol> findByDestination(String destination);
    Vol updatePlaces(Long id, int places);


}
