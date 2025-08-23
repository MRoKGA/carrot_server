package com.mrokga.carrot_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CarrotServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarrotServerApplication.class, args);
	}

}
