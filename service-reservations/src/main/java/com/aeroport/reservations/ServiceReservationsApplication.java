package com.aeroport.reservations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServiceReservationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceReservationsApplication.class, args);
	}

}
