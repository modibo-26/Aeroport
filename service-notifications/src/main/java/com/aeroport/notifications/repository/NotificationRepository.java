package com.aeroport.notifications.repository;

import com.aeroport.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByPassagerId(Long passagerId);
    List<Notification> findByPassagerIdOrderByDateCreationDesc(Long passagerId);
    List<Notification> findByVolId(Long volId);

}
