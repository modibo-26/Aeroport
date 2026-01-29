package com.aeroport.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolEvent {
    private Long volId;
    private String type;
    private String message;
}
