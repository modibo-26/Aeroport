package com.aeroport.vols.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vols")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;
    private String numeroVol;
    private String origine;
    private String destination;
    private LocalDateTime dateDepart;
    private LocalDateTime dateArrivee;
    private int placesDisponibles;
    private Double prixBase;
    private String compagnie;
    @Enumerated(EnumType.STRING)
    private Statut statut;
}
