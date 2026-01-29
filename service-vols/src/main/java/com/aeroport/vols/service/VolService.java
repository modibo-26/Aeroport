package com.aeroport.vols.service;

import com.aeroport.vols.dto.VolEvent;
import com.aeroport.vols.entity.Statut;
import com.aeroport.vols.entity.Vol;
import com.aeroport.vols.repository.VolRepository;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class VolService implements IVolService {

    private final VolRepository repository;

    private final KafkaTemplate<String, VolEvent> kafka;

    @Override
    public Vol AddVol(Vol vol) {
        vol.setStatut(Statut.A_L_HEURE);
        return repository.save(vol);
    }

    @Override
    public List<Vol> findAll() {
        return repository.findAll();
    }

    @Override
    public Vol findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vol non trouvé"));
    }

    @Override
    public Vol editVol(Long id, Vol volModifie) {
        Vol vol = findById(id);
        vol.setNumeroVol(volModifie.getNumeroVol());
        vol.setOrigine(volModifie.getOrigine());
        vol.setDestination(volModifie.getDestination());
        vol.setDateDepart(volModifie.getDateDepart());
        vol.setDateArrivee(volModifie.getDateArrivee());
        vol.setPlacesDisponibles(volModifie.getPlacesDisponibles());
        vol.setPrixBase(volModifie.getPrixBase());
        vol.setCompagnie(volModifie.getCompagnie());

        Vol saved = repository.save(vol);

        kafka.send("vol-events", new VolEvent(id, "MODIFIE", "Vol modifié"));

        return saved;
    }

    @Override
    public void deleteVol(Long id) {
        repository.deleteById(id);
    }

    @Override
    public int placeDisponible(Long id) {
        Vol vol = findById(id);
        return vol.getPlacesDisponibles();
    }

    @Override
    public Vol updateStatut(Long id, Statut statut) {
        Vol vol = findById(id);
        vol.setStatut(statut);
        Vol saved = repository.save(vol);

        kafka.send("vol-events", new VolEvent(id, statut.name(), "Statut changé : " + statut));

        return saved;
    }

    @Override
    public List<Vol> findByDestination(String destination) {
        return repository.findByDestination(destination);
    }
}
