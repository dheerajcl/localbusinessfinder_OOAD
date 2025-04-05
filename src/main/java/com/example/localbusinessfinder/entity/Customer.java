// src/main/java/com/example/localbusinessfinder/entity/Customer.java
package com.example.localbusinessfinder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.Set;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password; // Store hashed password

    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings;

    // Add constructors if needed (e.g., for registration)
    public Customer(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }
}
