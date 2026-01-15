package com.finance.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Advice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;          // Travel, Food, etc.
    private String message;           // Explanation
    private String suggestion;        // Action proposed
    private String confidence;        // LOW / MEDIUM / HIGH

    private String userDecision;      // ACCEPTED / REJECTED
    private String userReason;        // Optional feedback

    private LocalDateTime createdAt;

    public Advice() {
        this.createdAt = LocalDateTime.now();
    }

    // getters & setters (generate or paste)
    public Long getId() { return id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getUserDecision() { return userDecision; }
    public void setUserDecision(String userDecision) { this.userDecision = userDecision; }

    public String getUserReason() { return userReason; }
    public void setUserReason(String userReason) { this.userReason = userReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
