package com.example.localbusinessfinder.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDto {

    private Long id;

    @NotEmpty(message = "Business name is required")
    @Size(max = 255)
    private String name;

    @NotEmpty(message = "Category is required")
    @Size(max = 100)
    private String category;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotEmpty(message = "Address is required")
    @Size(max = 255)
    private String address;

    @NotEmpty(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotEmpty(message = "State is required")
    @Size(min = 2, max = 2, message = "State must be 2 characters")
    private String state; // e.g., "CA", "NY"

    @NotEmpty(message = "Zip code is required")
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid zip code format")
    private String zip;

    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$", message = "Invalid phone number format")
    private String phone;

    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$", message = "Invalid toll-free number format")
    private String tollFree; // Optional

    @NotNull(message = "Price range is required")
    @Min(value = 1, message = "Price range must be 1, 2, or 3")
    @Max(value = 3, message = "Price range must be 1, 2, or 3")
    private Integer priceRange; // 1 ($), 2 ($$), 3 ($$$)

    @NotNull(message = "Advance payment amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Advance amount cannot be negative")
    @Digits(integer = 6, fraction = 2, message = "Invalid currency format for advance amount")
    private BigDecimal advanceAmount;

    // Rating is calculated, not set directly via DTO usually
     private Double rating;

     // We link via the logged-in admin, not directly in DTO
     // private Long businessAdminId;
} 