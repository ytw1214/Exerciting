package com.exerciting.Exerciting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class ExercitingApplication {
	public static void main(String[] args) {
		SpringApplication.run(ExercitingApplication.class, args);
	}
}