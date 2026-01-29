package com.aeroport.notifications.controller;

import com.aeroport.notifications.entity.Notification;
import com.aeroport.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService service;

    @PostMapping
    public Notification addNotification(@RequestBody Notification notification) {
       return service.addNotification(notification);
    }

    @GetMapping("/passager/{passagerId}")
    public List<Notification> findByPassager(@PathVariable Long passagerId) {
       return service.findByPassagerId(passagerId);
   }

    @PutMapping("/{id}/lue")
    public Notification marqueLue(@PathVariable Long id){
        return service.marquerLue(id);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        service.deleteNotification(id);
    }

    @GetMapping("/vol/{volId}")
    public List<Notification> findByVol(@PathVariable Long volId) {
        return service.findByVolId(volId);
    }

}


