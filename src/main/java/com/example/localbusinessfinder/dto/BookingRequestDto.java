// src/main/java/com/example/localbusinessfinder/dto/BookingRequestDto.java
package com.example.localbusinessfinder.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class BookingRequestDto {
    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotNull(message = "Booking date and time are required")
    @FutureOrPresent(message = "Booking date must be in the present or future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // Expects yyyy-MM-dd'T'HH:mm
    private LocalDateTime bookingDate;

    @NotEmpty(message = "Issue description cannot be empty")
    private String issueDescription;

    // Advance amount will be determined by the business, not submitted by user
}