// src/main/java/com/example/localbusinessfinder/repository/BusinessRepository.java
package com.example.localbusinessfinder.repository;

import com.example.localbusinessfinder.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    // Search by Name (and check availability)
    @Query("SELECT b FROM Business b " +
           "LEFT JOIN b.bookings bk ON bk.bookingDate = :dateTime AND bk.status NOT IN ('CANCELLED_REFUNDED', 'CANCELLED_PENALTY') " +
           "WHERE LOWER(b.name) LIKE LOWER(concat('%', :name, '%')) AND bk.id IS NULL")
    List<Business> findByNameContainingIgnoreCaseAndAvailable(
        @Param("name") String name,
        @Param("dateTime") LocalDateTime dateTime
    );

    // Search by Criteria (Location, Category, Availability, Filters)
    @Query("SELECT DISTINCT b FROM Business b " +
           "LEFT JOIN b.bookings bk ON bk.business.id = b.id AND bk.bookingDate = :dateTime AND bk.status NOT IN ('CANCELLED_REFUNDED', 'CANCELLED_PENALTY') " +
           "WHERE (:city IS NULL OR LOWER(b.city) LIKE LOWER(concat('%', :city, '%'))) " +
           "AND (:category IS NULL OR b.category = :category) " +
           "AND (:maxPriceRange IS NULL OR b.priceRange <= :maxPriceRange) " +
           "AND (:minRating IS NULL OR b.rating >= :minRating) " +
           "AND bk.id IS NULL") // Ensures no conflicting booking at the exact time
    List<Business> findByCriteriaAndAvailable(
            @Param("city") String city,
            @Param("category") String category,
            @Param("dateTime") LocalDateTime dateTime,
            @Param("maxPriceRange") Integer maxPriceRange,
            @Param("minRating") Float minRating
    );

     // Simplified search by criteria without availability check (if needed separately)
     List<Business> findByCityContainingIgnoreCaseAndCategoryAndPriceRangeLessThanEqualAndRatingGreaterThanEqual(
            String city, String category, Integer priceRange, Float rating
     );

     List<Business> findByNameContainingIgnoreCase(String name);

}