// src/main/java/com/example/localbusinessfinder/repository/BookingRepository.java
package com.example.localbusinessfinder.repository;

import com.example.localbusinessfinder.entity.Booking;
import com.example.localbusinessfinder.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomer_IdOrderByBookingDateDesc(Long customerId);
    List<Booking> findByBusiness_IdOrderByBookingDateDesc(Long businessId);

    // Check for conflicting bookings for a specific business at a specific time
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.business.id = :businessId " +
           "AND b.bookingDate = :dateTime " +
           "AND b.status NOT IN ('CANCELLED_REFUNDED', 'CANCELLED_PENALTY')")
    boolean existsActiveBookingAtTime(
        @Param("businessId") Long businessId,
        @Param("dateTime") LocalDateTime dateTime
    );

     Optional<Booking> findByIdAndCustomer_Email(Long id, String customerEmail);
}