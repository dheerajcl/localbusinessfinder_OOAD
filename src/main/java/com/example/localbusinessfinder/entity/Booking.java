// src/main/java/com/example/localbusinessfinder/entity/Booking.java
package com.example.localbusinessfinder.entity;

import com.example.localbusinessfinder.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private LocalDateTime bookingDate; // The date/time of the service appointment

    @CreationTimestamp // Automatically set when the booking is created
    @Column(updatable = false)
    private LocalDateTime bookingTimestamp; // When the booking record was created

    @Lob
    private String issueDescription;

    @Column(precision = 10, scale = 2)
    private BigDecimal advancePaid;

    @Column(precision = 10, scale = 2)
    private BigDecimal adjustedPrice; // Total price set by business

    @Column(precision = 10, scale = 2)
    private BigDecimal finalPaid; // The remaining amount paid

    private String servicemanContact; // Could be fetched from Business or set per booking

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private BookingStatus status;
}
