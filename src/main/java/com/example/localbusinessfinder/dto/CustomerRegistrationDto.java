// src/main/java/com/example/localbusinessfinder/dto/CustomerRegistrationDto.java
package com.example.localbusinessfinder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // Lombok annotation for getters, setters, toString, equals, hashCode
public class CustomerRegistrationDto {
    @NotEmpty(message = "Name cannot be empty")
    private String name;

    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    private String confirmPassword; // For validation on the form
}