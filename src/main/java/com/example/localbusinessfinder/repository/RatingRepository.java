// src/main/java/com/example/localbusinessfinder/repository/RatingRepository.java
package com.example.localbusinessfinder.repository;

import com.example.localbusinessfinder.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByBusiness_Id(Long businessId);
    List<Rating> findByBusiness_IdOrderByRatingTimestampDesc(Long businessId);
    boolean existsByBooking_Id(Long bookingId);

    @Query("SELECT AVG(r.ratingValue) FROM Rating r WHERE r.business.id = :businessId")
    Optional<Double> getAverageRatingForBusiness(@Param("businessId") Long businessId);
}