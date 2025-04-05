// src/main/java/com/example/localbusinessfinder/service/BookingService.java
package com.example.localbusinessfinder.service;

import com.example.localbusinessfinder.dto.BookingRequestDto;
import com.example.localbusinessfinder.entity.Booking;
import com.example.localbusinessfinder.entity.Business;
import com.example.localbusinessfinder.entity.Customer;
import com.example.localbusinessfinder.enums.BookingStatus;
import com.example.localbusinessfinder.repository.BookingRepository;
import com.example.localbusinessfinder.repository.BusinessRepository;
import com.example.localbusinessfinder.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Value("${app.cancellation.grace-hours:3}") // Default to 3 if not set
    private int cancellationGraceHours;

    @Transactional
    public Booking createBooking(BookingRequestDto requestDto, String customerEmail) {
        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        Business business = businessRepository.findById(requestDto.getBusinessId())
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        // Double-check availability at booking time (prevent race conditions)
        if (bookingRepository.existsActiveBookingAtTime(business.getId(), requestDto.getBookingDate())) {
            throw new IllegalStateException("Time slot is no longer available for " + business.getName());
        }

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setBusiness(business);
        booking.setBookingDate(requestDto.getBookingDate());
        booking.setIssueDescription(requestDto.getIssueDescription());
        booking.setAdvancePaid(business.getAdvanceAmount()); // Set advance paid
        booking.setStatus(BookingStatus.CONFIRMED); // Initial status
        booking.setServicemanContact(business.getTollFree()); // Simulate: Use toll-free as contact for now

        // --- Payment Simulation ---
        System.out.printf("SIMULATION: Processing advance payment of $%.2f for booking.\n", business.getAdvanceAmount());
        // In a real app, integrate with a payment gateway here.

        return bookingRepository.save(booking);
    }

     public List<Booking> findBookingsByCustomerEmail(String customerEmail) {
        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        return bookingRepository.findByCustomer_IdOrderByBookingDateDesc(customer.getId());
    }

    public Optional<Booking> findBookingByIdAndCustomerEmail(Long bookingId, String customerEmail) {
        logger.debug("Searching for booking ID {} owned by customer {}", bookingId, customerEmail);
        // Consider adding more specific logging if issues persist
        return bookingRepository.findByIdAndCustomer_Email(bookingId, customerEmail);
    }

     public Optional<Booking> findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, String customerEmail) {
        Booking booking = findBookingByIdAndCustomerEmail(bookingId, customerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found or access denied"));

        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.AWAITING_FINAL_PAYMENT ) {
             throw new IllegalStateException("Booking cannot be cancelled in its current state: " + booking.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        // Use bookingTimestamp (when booking was made) for grace period calculation
        LocalDateTime cancellationDeadline = booking.getBookingTimestamp().plusHours(cancellationGraceHours);

        if (now.isBefore(cancellationDeadline)) {
            // --- Refund Simulation ---
            System.out.printf("SIMULATION: Processing refund of $%.2f for booking ID %d.\n", booking.getAdvancePaid(), booking.getId());
            booking.setStatus(BookingStatus.CANCELLED_REFUNDED);
            // In a real app, trigger refund via payment gateway.
        } else {
            // --- Penalty Simulation ---
            System.out.printf("SIMULATION: Retaining advance payment of $%.2f as penalty for booking ID %d.\n", booking.getAdvancePaid(), booking.getId());
            booking.setStatus(BookingStatus.CANCELLED_PENALTY);
            // No refund needed, advance is kept.
        }

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking setAdjustedPrice(Long bookingId, BigDecimal adjustedPrice) {
         // In a real app, this might be triggered by a business user interface
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
             throw new IllegalStateException("Adjusted price can only be set for confirmed bookings.");
        }
        if (adjustedPrice.compareTo(booking.getAdvancePaid()) < 0) {
             throw new IllegalArgumentException("Adjusted price cannot be less than the advance paid.");
        }

        booking.setAdjustedPrice(adjustedPrice);
        booking.setStatus(BookingStatus.AWAITING_FINAL_PAYMENT);

        System.out.printf("SIMULATION: Adjusted price set to $%.2f for booking ID %d.\n", adjustedPrice, booking.getId());

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking recordFinalPayment(Long bookingId, String customerEmail) {
        Booking booking = findBookingByIdAndCustomerEmail(bookingId, customerEmail)
             .orElseThrow(() -> new EntityNotFoundException("Booking not found or access denied"));

        if (booking.getStatus() != BookingStatus.AWAITING_FINAL_PAYMENT) {
            throw new IllegalStateException("Final payment can only be made when status is AWAITING_FINAL_PAYMENT.");
        }
        if (booking.getAdjustedPrice() == null || booking.getAdjustedPrice().compareTo(BigDecimal.ZERO) <= 0) {
             throw new IllegalStateException("Adjusted price not set for this booking.");
        }

        BigDecimal amountDue = booking.getAdjustedPrice().subtract(booking.getAdvancePaid());
        if (amountDue.compareTo(BigDecimal.ZERO) < 0) {
            // Should not happen if validation in setAdjustedPrice is correct, but double-check
             amountDue = BigDecimal.ZERO;
        }

        // --- Final Payment Simulation ---
        System.out.printf("SIMULATION: Processing final payment of $%.2f for booking ID %d.\n", amountDue, booking.getId());
        // In a real app, integrate with payment gateway here.
        System.out.printf("SIMULATION: Disbursing total amount of $%.2f to business '%s'.\n", booking.getAdjustedPrice(), booking.getBusiness().getName());


        booking.setFinalPaid(amountDue);
        booking.setStatus(BookingStatus.COMPLETED_PENDING_RATING); // Move to next status

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking markBookingAsCompleted(Long bookingId, String customerEmail) {
        logger.info("Attempting to mark booking {} as completed for customer {}", bookingId, customerEmail);
        Booking booking = findBookingByIdAndCustomerEmail(bookingId, customerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found or access denied."));

        // Verify status is CONFIRMED
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            logger.warn("Cannot mark booking {} as completed: Status is {}", bookingId, booking.getStatus());
            throw new IllegalStateException("Booking cannot be marked as completed (Status: " + booking.getStatus() + ").");
        }

        // Verify booking date is in the past
        if (booking.getBookingDate() == null || booking.getBookingDate().isAfter(LocalDateTime.now())) {
             logger.warn("Cannot mark booking {} as completed: Booking date {} is in the future.", bookingId, booking.getBookingDate());
            throw new IllegalStateException("Booking cannot be marked as completed until after the scheduled date/time.");
        }

        // Set a default adjusted price (e.g., double the advance, or just the advance)
        // Let's assume the default is just double the advance for simplicity here.
        // You might want more complex logic or require business input later.
        if (booking.getAdjustedPrice() == null || booking.getAdjustedPrice().compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal defaultFinal = booking.getAdvancePaid().multiply(new BigDecimal("2.0"));
            booking.setAdjustedPrice(defaultFinal);
            logger.info("Setting default adjusted price for booking {} to {}", bookingId, defaultFinal);
        } else {
             logger.info("Booking {} already has an adjusted price: {}", bookingId, booking.getAdjustedPrice());
        }


        // Update status
        booking.setStatus(BookingStatus.AWAITING_FINAL_PAYMENT);
        logger.info("Updating booking {} status to AWAITING_FINAL_PAYMENT", bookingId);

        return bookingRepository.save(booking);
    }

}
