package com.tarpha.torrssen2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Torrssen2Application {
	
	public static void main(String[] args) {
		SpringApplication.run(Torrssen2Application.class, args);
	}

}
