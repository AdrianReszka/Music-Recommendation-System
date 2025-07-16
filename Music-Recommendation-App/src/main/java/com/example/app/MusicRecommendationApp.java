package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.awt.*;
import java.net.URI;

@EnableJpaRepositories(basePackages = "com.example.repository")
@SpringBootApplication(scanBasePackages = {"com.example", "com.example.controller"})
@EntityScan(basePackages = "com.example.model")
public class MusicRecommendationApp {

	public static void main(String[] args) {
		SpringApplication.run(MusicRecommendationApp.class, args);

	}
}
