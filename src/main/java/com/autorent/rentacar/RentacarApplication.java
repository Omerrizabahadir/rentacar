package com.autorent.rentacar;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.autorent.rentacar.model") // Entity'lerinizin bulunduğu paket
public class RentacarApplication  {

	public static void main(String[] args) {
		SpringApplication.run(RentacarApplication.class, args);
	}


}
