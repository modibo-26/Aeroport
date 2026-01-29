package com.aeroport.notifications.service;

import com.aeroport.notifications.entity.Notification;

import java.util.List;

public interface INotificationService {
    Notification addNotification(Notification notification);
    List<Notification> findByPassagerId(Long passagerId);
    Notification marquerLue(Long id);
    void deleteNotification(Long id);
    List<Notification> findByVolId(Long volId);
}
