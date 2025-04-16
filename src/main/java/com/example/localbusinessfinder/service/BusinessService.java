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

import com.example.localbusinessfinder.dto.BusinessDto;
import com.example.localbusinessfinder.entity.BusinessAdmin;
import com.example.localbusinessfinder.repository.BusinessAdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityNotFoundException;

@Service
public class BusinessService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessService.class);

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private BusinessAdminRepository businessAdminRepository;

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

    // **** Find Business by Admin Email ****
    public Optional<Business> findBusinessByAdminEmail(String adminEmail) {
        logger.debug("Searching for business owned by admin email: {}", adminEmail);
        return businessRepository.findByBusinessAdmin_Email(adminEmail);
    }

    // **** Create Business linked to Admin ****
    @Transactional
    public Business createBusiness(BusinessDto businessDto, String adminEmail) {
        BusinessAdmin admin = businessAdminRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new EntityNotFoundException("Business Admin not found with email: " + adminEmail));

        // Check if this admin already has a business (assuming one-to-one for now)
        if (businessRepository.findByBusinessAdmin_Email(adminEmail).isPresent()) {
             logger.warn("Admin {} attempted to create a second business.", adminEmail);
             throw new IllegalStateException("You can only manage one business listing with this account.");
        }

        Business business = convertDtoToEntity(businessDto);
        business.setBusinessAdmin(admin); // Link to the admin

        Business savedBusiness = businessRepository.save(business);
        logger.info("Admin {} created new business: {}", adminEmail, savedBusiness.getId());
        return savedBusiness;
    }

    // **** Update Business owned by Admin ****
    @Transactional
    public Business updateBusiness(BusinessDto businessDto, String adminEmail) {
         BusinessAdmin admin = businessAdminRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new EntityNotFoundException("Business Admin not found with email: " + adminEmail));

         // Find the existing business owned by this admin
         Business existingBusiness = businessRepository.findByBusinessAdmin_Email(adminEmail)
                 .orElseThrow(() -> new EntityNotFoundException("No business listing found for admin: " + adminEmail));

         // Ensure the DTO ID matches the existing business ID if provided
         if (businessDto.getId() != null && !businessDto.getId().equals(existingBusiness.getId())) {
             logger.error("Admin {} attempted to update business with mismatched ID. Expected: {}, Got in DTO: {}", adminEmail, existingBusiness.getId(), businessDto.getId());
             throw new IllegalArgumentException("Business ID mismatch during update.");
         }

         // Update fields from DTO
         existingBusiness.setName(businessDto.getName());
         existingBusiness.setCategory(businessDto.getCategory());
         existingBusiness.setDescription(businessDto.getDescription());
         existingBusiness.setAddress(businessDto.getAddress());
         existingBusiness.setCity(businessDto.getCity());
         existingBusiness.setState(businessDto.getState());
         existingBusiness.setZip(businessDto.getZip());
         existingBusiness.setPhone(businessDto.getPhone());
         existingBusiness.setTollFree(businessDto.getTollFree());
         existingBusiness.setPriceRange(businessDto.getPriceRange());
         existingBusiness.setAdvanceAmount(businessDto.getAdvanceAmount());
         // DO NOT update admin link here

         Business updatedBusiness = businessRepository.save(existingBusiness);
         logger.info("Admin {} updated business: {}", adminEmail, updatedBusiness.getId());
         return updatedBusiness;
    }


    // **** Helper to convert DTO to Entity (adjust as needed) ****
    private Business convertDtoToEntity(BusinessDto dto) {
        Business business = new Business();
        // Copy properties from DTO
        business.setName(dto.getName());
        business.setCategory(dto.getCategory());
        business.setDescription(dto.getDescription());
        business.setAddress(dto.getAddress());
        business.setCity(dto.getCity());
        business.setState(dto.getState());
        business.setZip(dto.getZip());
        business.setPhone(dto.getPhone());
        business.setTollFree(dto.getTollFree());
        business.setPriceRange(dto.getPriceRange());
        business.setAdvanceAmount(dto.getAdvanceAmount());
        // Rating is calculated, not set from DTO
        // Admin is set separately
        return business;
    }

     // **** Helper to convert Entity to DTO (adjust as needed) ****
     public BusinessDto convertEntityToDto(Business business) {
        if (business == null) return null;
         BusinessDto dto = new BusinessDto();
         dto.setId(business.getId());
         dto.setName(business.getName());
         dto.setCategory(business.getCategory());
         dto.setDescription(business.getDescription());
         dto.setAddress(business.getAddress());
         dto.setCity(business.getCity());
         dto.setState(business.getState());
         dto.setZip(business.getZip());
         dto.setPhone(business.getPhone());
         dto.setTollFree(business.getTollFree());
         dto.setPriceRange(business.getPriceRange());
         dto.setAdvanceAmount(business.getAdvanceAmount());
         dto.setRating(business.getRating() != null ? business.getRating().doubleValue() : null);
         return dto;
     }
}