package com.finance.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class MLService {

    private final WebClient webClient;

    public MLService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8000")
                .build();
    }

    public String predictCategory(String description) {
        Map<String, String> request = Map.of("description", description);

        Map response = webClient.post()
                .uri("/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return response.get("category").toString();
    }
}

