package com.aeroport.notifications.service;

import com.aeroport.notifications.entity.Notification;
import com.aeroport.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {
    private final NotificationRepository repository;

    @Override
    public Notification addNotification(Notification notification) {
        notification.setLue(false);
        notification.setDateCreation(LocalDateTime.now());
        return repository.save(notification);
    }

    @Override
    public List<Notification> findByPassagerId(Long passagerId) {
        return repository.findByPassagerId(passagerId);
    }

    @Override
    public Notification marquerLue(Long id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée"));
        notification.setLue(true);
        return repository.save(notification);
    }

    @Override
    public void deleteNotification(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<Notification> findByVolId(Long volId) {
        return repository.findByVolId(volId);
    }
}
