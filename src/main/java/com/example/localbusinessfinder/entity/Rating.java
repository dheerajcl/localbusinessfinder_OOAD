// src/main/java/com/example/localbusinessfinder/entity/Rating.java
package com.example.localbusinessfinder.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@Getter
@Setter
@NoArgsConstructor
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to booking to ensure rating is for a completed service
    @OneToOne // Assuming one rating per booking
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    @Min(1) @Max(5)
    private Integer ratingValue; // Renamed from 'rating'

    @Lob
    private String comment;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime ratingTimestamp;
}