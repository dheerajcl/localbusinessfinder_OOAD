// src/main/java/com/example/localbusinessfinder/controller/BookingController.java
package com.example.localbusinessfinder.controller;

import com.example.localbusinessfinder.dto.BookingRequestDto;
import com.example.localbusinessfinder.entity.Booking;
import com.example.localbusinessfinder.entity.Business;
import com.example.localbusinessfinder.service.BookingService;
import com.example.localbusinessfinder.service.BusinessService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.localbusinessfinder.enums.BookingStatus;

@Controller
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BusinessService businessService;

    @Value("${app.cancellation.grace-hours:3}")
    private int cancellationGraceHours;

    @GetMapping("/book/{businessId}")
    public String showBookingForm(@PathVariable Long businessId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
                                  Model model, Principal principal) {

         if (principal == null) return "redirect:/login";

        Business business = businessService.findBusinessById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        BookingRequestDto bookingRequest = new BookingRequestDto();
        bookingRequest.setBusinessId(businessId);
        bookingRequest.setBookingDate(dateTime);

        model.addAttribute("bookingRequest", bookingRequest);
        model.addAttribute("business", business);
        return "booking-form"; // booking-form.html
    }

    @PostMapping("/book")
    public String processBooking(@Valid @ModelAttribute("bookingRequest") BookingRequestDto bookingRequest,
                                BindingResult result, Model model, Principal principal, RedirectAttributes redirectAttributes) {

         if (principal == null) return "redirect:/login";
         if (result.hasErrors()) {
            // Reload business data if there are errors
            Business business = businessService.findBusinessById(bookingRequest.getBusinessId()).orElse(null);
            model.addAttribute("business", business);
             model.addAttribute("bookingRequest", bookingRequest); // Keep data
            return "booking-form";
        }

        try {
            String customerEmail = principal.getName();
            Booking newBooking = bookingService.createBooking(bookingRequest, customerEmail);
            redirectAttributes.addFlashAttribute("booking", newBooking);
            redirectAttributes.addFlashAttribute("cancellationGraceHours", cancellationGraceHours);
            return "redirect:/booking/confirmation/" + newBooking.getId(); // Redirect to confirmation page
        } catch (IllegalStateException | EntityNotFoundException e) {
            // Handle specific errors like slot taken or entity not found
            Business business = businessService.findBusinessById(bookingRequest.getBusinessId()).orElse(null);
            model.addAttribute("business", business);
            model.addAttribute("bookingRequest", bookingRequest);
            model.addAttribute("errorMessage", e.getMessage());
            return "booking-form";
        } catch (Exception e) {
            // Generic error handling
             Business business = businessService.findBusinessById(bookingRequest.getBusinessId()).orElse(null);
             model.addAttribute("business", business);
             model.addAttribute("bookingRequest", bookingRequest);
             model.addAttribute("errorMessage", "An unexpected error occurred during booking.");
             e.printStackTrace(); // Log the full error
             return "booking-form";
        }
    }

    @GetMapping("/booking/confirmation/{bookingId}")
    public String showBookingConfirmation(@PathVariable Long bookingId, Model model, Principal principal, RedirectAttributes redirectAttributes) {
         if (principal == null) return "redirect:/login";

         // Check if booking details were passed via flash attributes (preferred after redirect)
         if (!model.containsAttribute("booking")) {
             // If not, fetch from DB (ensure user owns booking)
             Booking booking = bookingService.findBookingByIdAndCustomerEmail(bookingId, principal.getName())
                      .orElseThrow(() -> new EntityNotFoundException("Booking not found or access denied."));
             model.addAttribute("booking", booking);
             model.addAttribute("cancellationGraceHours", cancellationGraceHours); // Add grace period again if fetched
         }

         // Add cancellation deadline if booking exists and has timestamp
        if (model.containsAttribute("booking")) {
            Booking booking = (Booking) model.getAttribute("booking");
            if (booking != null && booking.getBookingTimestamp() != null) {
                 LocalDateTime deadline = booking.getBookingTimestamp().plusHours(cancellationGraceHours);
                 model.addAttribute("cancellationDeadline", deadline);
            }
        }


         return "booking-confirmation"; // booking-confirmation.html
    }

    @GetMapping("/bookings/{bookingId}")
    public String showBookingDetails(@PathVariable Long bookingId, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        logger.info("Attempting to view details for booking ID: {}", bookingId);
        if (principal == null) {
            logger.warn("User is not authenticated. Redirecting to login.");
            return "redirect:/login";
        }
        logger.info("User authenticated as: {}", principal.getName());

        try {
            logger.debug("Calling bookingService.findBookingByIdAndCustomerEmail for ID: {} and Email: {}", bookingId, principal.getName());
            Booking booking = bookingService.findBookingByIdAndCustomerEmail(bookingId, principal.getName())
                    .orElseThrow(() -> {
                        logger.warn("Booking not found or access denied for ID: {} and Email: {}", bookingId, principal.getName());
                        return new EntityNotFoundException("Booking not found or access denied. ID: " + bookingId);
                    });

            logger.info("Booking found: {}", booking);

            model.addAttribute("booking", booking);
            if (booking.getBookingTimestamp() != null) {
                 LocalDateTime deadline = booking.getBookingTimestamp().plusHours(cancellationGraceHours);
                 model.addAttribute("cancellationDeadline", deadline);
                 model.addAttribute("now", LocalDateTime.now());
                 model.addAttribute("cancellationGraceHours", cancellationGraceHours);
            }

            return "booking-detail"; // booking-detail.html
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bookings/my"; // Redirect back to list if not found/denied
        } catch (Exception e) {
             logger.error("Unexpected error fetching booking details ID {}: {}", bookingId, e.getMessage(), e);
             redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while loading booking details.");
             return "redirect:/bookings/my"; // Redirect back to list on other errors
        }
    }

    @GetMapping("/bookings/my")
    public String showMyBookings(Model model, Principal principal) {
         if (principal == null) return "redirect:/login";

        String customerEmail = principal.getName();
        List<Booking> bookings = bookingService.findBookingsByCustomerEmail(customerEmail);
        model.addAttribute("bookings", bookings);
        model.addAttribute("now", LocalDateTime.now()); // For enabling/disabling actions
        model.addAttribute("cancellationGraceHours", cancellationGraceHours);

         // Add data needed for simulations or actions
        model.addAttribute("canSimulateAdjustment", true); // Flag to show simulation button

        return "my-bookings"; // my-bookings.html
    }


    @PostMapping("/bookings/{bookingId}/cancel")
    public String cancelBooking(@PathVariable Long bookingId, Principal principal, RedirectAttributes redirectAttributes) {
         if (principal == null) return "redirect:/login";

        try {
            Booking cancelledBooking = bookingService.cancelBooking(bookingId, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Booking cancelled successfully. Status: " + cancelledBooking.getStatus());
        } catch (EntityNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cancellation failed: " + e.getMessage());
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred during cancellation.");
             e.printStackTrace();
        }
        return "redirect:/bookings/my";
    }


     // --- SIMULATION ENDPOINT for Business adjusting price ---
    @PostMapping("/bookings/{bookingId}/simulate-adjust")
    public String simulateAdjustPrice(@PathVariable Long bookingId, RedirectAttributes redirectAttributes) {
        try {
            redirectAttributes.addFlashAttribute("successMessage", "Simulated price adjustment successful for booking " + bookingId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Simulation failed: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/bookings/my";
    }


    @GetMapping("/bookings/{bookingId}/pay")
    public String showPaymentPage(@PathVariable Long bookingId, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        try {
            Booking booking = bookingService.findBookingByIdAndCustomerEmail(bookingId, principal.getName())
                    .orElseThrow(() -> new EntityNotFoundException("Booking not found or access denied."));

             // Ensure it's ready for final payment
             if (booking.getStatus() != BookingStatus.AWAITING_FINAL_PAYMENT) {
                  model.addAttribute("errorMessage", "Final payment is not due or possible for this booking currently (Status: " + booking.getStatus() + ").");
                  model.addAttribute("booking", booking); // Still show details
                  return "payment-due"; // Or show payment page with error message
             }


            BigDecimal amountDue = booking.getAdjustedPrice().subtract(booking.getAdvancePaid());
             if (amountDue.compareTo(BigDecimal.ZERO) < 0) {
                  amountDue = BigDecimal.ZERO; // Cannot be negative
             }

            model.addAttribute("booking", booking);
            model.addAttribute("amountDue", amountDue);
            return "payment-due"; // payment-due.html - Make sure this template exists and has a form POSTing to /payFinal
        } catch (EntityNotFoundException e) {
             model.addAttribute("errorMessage", e.getMessage()); // Add error to model if returning view directly
             return "redirect:/bookings/my"; // Redirect if booking not found/accessible
        } catch (Exception e) {
             model.addAttribute("errorMessage", "An unexpected error occurred preparing the payment page.");
             e.printStackTrace();
             return "redirect:/bookings/my"; // Redirect on generic error
        }
    }

    @PostMapping("/bookings/{bookingId}/payFinal")
    public String processFinalPayment(@PathVariable Long bookingId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        try {
            Booking paidBooking = bookingService.recordFinalPayment(bookingId, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Final payment successful! Booking status: " + paidBooking.getStatus());
            // Redirect to rating page or back to my bookings
            return "redirect:/bookings/" + bookingId + "/rate"; // Redirect to rating page
        } catch (EntityNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Final payment failed: " + e.getMessage());
            // **** Correct Redirect: Go back to the GET payment page ****
            return "redirect:/bookings/" + bookingId + "/pay"; // Go back to payment page with error
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred during final payment.");
            e.printStackTrace();
            return "redirect:/bookings/my"; // Redirect to bookings list on generic error
        }
    }

    // **** ADD NEW ENDPOINT for Marking Completed ****
    @PostMapping("/bookings/{bookingId}/mark-completed")
    public String markBookingCompleted(@PathVariable Long bookingId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        logger.info("User {} attempting to mark booking {} as completed.", principal.getName(), bookingId);

        try {
            bookingService.markBookingAsCompleted(bookingId, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Booking marked as completed. Ready for final payment.");
            logger.info("Booking {} successfully marked as completed by user {}.", bookingId, principal.getName());
            // Redirect back to my bookings to see the status change and Pay button
            return "redirect:/bookings/my";
        } catch (EntityNotFoundException | IllegalStateException e) {
            logger.warn("Failed to mark booking {} as completed for user {}: {}", bookingId, principal.getName(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Could not mark booking as completed: " + e.getMessage());
            return "redirect:/bookings/my"; // Stay on my bookings page
        } catch (Exception e) {
            logger.error("Unexpected error marking booking {} as completed for user {}: {}", bookingId, principal.getName(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred.");
            return "redirect:/bookings/my";
        }
    }
    // **** END NEW ENDPOINT ****
}