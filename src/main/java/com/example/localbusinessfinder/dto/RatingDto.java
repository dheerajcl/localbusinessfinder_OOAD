// src/main/java/com/example/localbusinessfinder/dto/RatingDto.java
package com.example.localbusinessfinder.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RatingDto {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Rating value is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer ratingValue;

    private String comment;
}