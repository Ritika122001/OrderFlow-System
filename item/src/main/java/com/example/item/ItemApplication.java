package com.example.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ItemApplication {
	public static void main(String[] args) {
		SpringApplication.run(ItemApplication.class, args);
	}

}
