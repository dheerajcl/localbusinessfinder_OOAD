// src/main/java/com/example/localbusinessfinder/entity/Business.java
package com.example.localbusinessfinder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "businesses")
@Getter
@Setter
@NoArgsConstructor
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;
    private String city;
    private String state;
    private String zip;
    private String category;
    private String tollFree;

    @Column(name = "price_range")
    private Integer priceRange; // 1, 2, 3 for $, $$, $$$

    @Column(columnDefinition = "FLOAT DEFAULT 0")
    private Float rating = 0.0f;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal advanceAmount;

    @Lob // For potentially long text
    private String description;

    @Column(name = "phone")
    private String phone;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Rating> ratings = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_admin_id", referencedColumnName = "id")
    private BusinessAdmin businessAdmin;
}