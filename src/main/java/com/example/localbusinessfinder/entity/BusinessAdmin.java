package com.example.localbusinessfinder.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "business_admins")
public class BusinessAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Size(min = 2, max = 100)
    @Column(nullable = false)
    private String name;

    @NotEmpty
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotEmpty
    @Size(min = 6) // Consider stronger password requirements in production
    @Column(nullable = false)
    private String password; // Store hashed password

    @Column(nullable = false)
    private String roles = "ROLE_BUSINESS_ADMIN"; // Default role

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Relationship: One admin might manage one or more businesses
    // Let's start with One-to-One for simplicity: One Admin -> One Business
    // If you need One-to-Many later, change this mapping
    @OneToOne(mappedBy = "businessAdmin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Business business;
} 