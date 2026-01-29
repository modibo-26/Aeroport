package com.aeroport.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServiceNotificationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceNotificationsApplication.class, args);
	}

}
