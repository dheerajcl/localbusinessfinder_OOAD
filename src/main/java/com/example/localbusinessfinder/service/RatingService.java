// src/main/java/com/example/localbusinessfinder/service/RatingService.java
package com.example.localbusinessfinder.service;

import com.example.localbusinessfinder.dto.RatingDto;
import com.example.localbusinessfinder.entity.Booking;
import com.example.localbusinessfinder.entity.Business;
import com.example.localbusinessfinder.entity.Customer;
import com.example.localbusinessfinder.entity.Rating;
import com.example.localbusinessfinder.enums.BookingStatus;
import com.example.localbusinessfinder.repository.BookingRepository;
import com.example.localbusinessfinder.repository.CustomerRepository;
import com.example.localbusinessfinder.repository.RatingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatingService {

    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BusinessService businessService; // To update business average rating

    @Transactional
    public Rating submitRating(RatingDto ratingDto, String customerEmail) {
        Booking booking = bookingRepository.findByIdAndCustomer_Email(ratingDto.getBookingId(), customerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found or you are not authorized to rate this booking."));

        // Ensure booking is in a state where rating is allowed (e.g., Fully Paid)
        if (booking.getStatus() != BookingStatus.FULLY_PAID && booking.getStatus() != BookingStatus.COMPLETED_PENDING_RATING) {
             throw new IllegalStateException("You can only rate a fully paid booking. Current status: " + booking.getStatus());
        }

        // Prevent duplicate ratings for the same booking
        if (ratingRepository.existsByBooking_Id(booking.getId())) {
            throw new IllegalStateException("A rating has already been submitted for this booking.");
        }

        Business business = booking.getBusiness(); // Get business from the booking

        Rating rating = new Rating();
        rating.setBooking(booking);
        rating.setBusiness(business);
        rating.setRatingValue(ratingDto.getRatingValue());
        rating.setComment(ratingDto.getComment());

        Rating savedRating = ratingRepository.save(rating);

        // Update the average rating for the business
        businessService.updateBusinessRating(business.getId());

        // Optionally update booking status if using COMPLETED_PENDING_RATING
        // booking.setStatus(BookingStatus.COMPLETED_RATED);
        // bookingRepository.save(booking);

        return savedRating;
    }

    public boolean hasRatingForBooking(Long bookingId) {
       return ratingRepository.existsByBooking_Id(bookingId);
    }

    @Transactional
    public Rating createRating(RatingDto ratingDto, String customerEmail) {
        logger.info("Attempting to create rating for booking ID {} by customer {}", ratingDto.getBookingId(), customerEmail);

        // 1. Fetch Booking and Customer
        Booking booking = bookingRepository.findById(ratingDto.getBookingId())
                .orElseThrow(() -> {
                    logger.warn("Rating creation failed: Booking not found with ID {}", ratingDto.getBookingId());
                    return new EntityNotFoundException("Booking not found with ID: " + ratingDto.getBookingId());
                });

        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> {
                     logger.warn("Rating creation failed: Customer not found with email {}", customerEmail);
                    return new EntityNotFoundException("Customer not found with email: " + customerEmail);
                });

        // 2. Validate Ownership
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            logger.warn("Rating creation failed: Booking {} does not belong to customer {}", ratingDto.getBookingId(), customerEmail);
            throw new IllegalStateException("You can only rate your own bookings.");
        }

        // 3. Validate Booking Status
        if (booking.getStatus() != BookingStatus.COMPLETED_PENDING_RATING && booking.getStatus() != BookingStatus.FULLY_PAID) {
             logger.warn("Rating creation failed: Booking {} has invalid status {} for rating.", ratingDto.getBookingId(), booking.getStatus());
            throw new IllegalStateException("Booking cannot be rated in its current status: " + booking.getStatus());
        }

        // 4. Check if Already Rated
        if (hasRatingForBooking(booking.getId())) {
             logger.warn("Rating creation failed: Booking {} has already been rated.", ratingDto.getBookingId());
            throw new IllegalStateException("This booking has already been rated.");
        }

        // 5. Create and Save Rating
        Rating rating = new Rating();
        rating.setBooking(booking);
        // rating.setCustomer(customer); // Removed this line - Customer is linked via Booking
        rating.setBusiness(booking.getBusiness()); // Link to the business as well
        rating.setRatingValue(ratingDto.getRatingValue());
        rating.setComment(ratingDto.getComment());
        // ratingTimestamp is set automatically by @CreationTimestamp

        Rating savedRating = ratingRepository.save(rating);
        logger.info("Rating successfully created with ID {} for booking {}", savedRating.getId(), booking.getId());

        // Update the average rating for the business
        Business business = booking.getBusiness();
        businessService.updateBusinessRating(business.getId());

        // Update Booking Status to COMPLETED_RATED
        booking.setStatus(BookingStatus.COMPLETED_RATED);
        bookingRepository.save(booking);
        logger.info("Updated booking {} status to COMPLETED_RATED after rating.", booking.getId());

        return savedRating;
    }

    // Add method to get all ratings for a business
    public List<Rating> getBusinessRatings(Long businessId) {
        logger.info("Getting all ratings for business ID: {}", businessId);
        return ratingRepository.findByBusiness_IdOrderByRatingTimestampDesc(businessId);
    }
}