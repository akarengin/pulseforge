package com.akarengin.pulseforge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

// Maps to 'events' table in PostgreSQL
@Entity
@Table(name = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    // Stored as String, PostgreSQL handles JSONB serialization
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(nullable = false)
    private Instant timestamp;

    // JPA lifecycle callback: automatically sets timestamp before first save
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
