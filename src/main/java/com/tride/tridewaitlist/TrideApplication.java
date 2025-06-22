package com.tride.tridewaitlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.WebClient;


@SpringBootApplication
@EnableAsync
@EnableScheduling
public class TrideApplication {
    private static final Logger log = LoggerFactory.getLogger(TrideApplication.class);
    private final WebClient webClient;

    public TrideApplication(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://tride-waitlist-service.onrender.com").build();
    }

    public static void main(String[] args) {
        SpringApplication.run(TrideApplication.class, args);
    }

    @Scheduled(fixedRate = 300000)
    public void keepServerAlive() {
        webClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> log.info("Ping successful: {}", response),
                        error -> log.error("Ping failed: {}", error.getMessage())
                );
    }
}