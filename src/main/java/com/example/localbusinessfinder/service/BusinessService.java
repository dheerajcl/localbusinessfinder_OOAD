// src/main/java/com/example/localbusinessfinder/service/BusinessService.java
package com.example.localbusinessfinder.service;

import com.example.localbusinessfinder.entity.Business;
import com.example.localbusinessfinder.repository.BusinessRepository;
import com.example.localbusinessfinder.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BusinessService {

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private RatingRepository ratingRepository;


    public List<Business> searchBusinesses(String name, String city, String category, LocalDateTime dateTime, Integer maxPriceRange, Float minRating) {
        if (name != null && !name.isBlank()) {
            // Option 2: Search primarily by name, checking availability
            return businessRepository.findByNameContainingIgnoreCaseAndAvailable(name, dateTime);
        } else {
            // Option 1: Search by criteria, checking availability
             // Ensure null checks or defaults if criteria are optional
            String searchCity = (city != null && !city.isBlank()) ? city : null;
            String searchCategory = (category != null && !category.isBlank()) ? category : null;

            return businessRepository.findByCriteriaAndAvailable(searchCity, searchCategory, dateTime, maxPriceRange, minRating);
        }
    }

    public Optional<Business> findBusinessById(Long id) {
        return businessRepository.findById(id);
    }

    @Transactional // Ensure atomic update
    public void updateBusinessRating(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found: " + businessId));

        // Calculate average rating from the ratings table
        Double averageRating = ratingRepository.getAverageRatingForBusiness(businessId).orElse(0.0);

        // Update the rating on the business entity (round to one decimal place)
        business.setRating((float) Math.round(averageRating * 10) / 10);
        businessRepository.save(business);
    }

     // Method to potentially add businesses (for setup/testing)
    @Transactional
    public Business addBusiness(Business business) {
        // Add validation if needed
        return businessRepository.save(business);
    }
}