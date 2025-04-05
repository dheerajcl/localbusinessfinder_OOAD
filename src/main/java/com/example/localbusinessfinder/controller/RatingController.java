// src/main/java/com/example/localbusinessfinder/controller/RatingController.java
package com.example.localbusinessfinder.controller;

import com.example.localbusinessfinder.dto.RatingDto;
import com.example.localbusinessfinder.entity.Booking;
import com.example.localbusinessfinder.enums.BookingStatus;
import com.example.localbusinessfinder.service.BookingService;
import com.example.localbusinessfinder.service.RatingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/bookings/{bookingId}/rate")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public String showRatingForm(@PathVariable Long bookingId, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        String customerEmail = principal.getName();

        try {
            Booking booking = bookingService.findBookingByIdAndCustomerEmail(bookingId, customerEmail)
                    .orElseThrow(() -> new EntityNotFoundException("Booking not found or access denied."));

            // Check if rating is allowed based on status
            if (booking.getStatus() != BookingStatus.FULLY_PAID && booking.getStatus() != BookingStatus.COMPLETED_PENDING_RATING) {
                 model.addAttribute("errorMessage", "Rating is not available for this booking (Status: " + booking.getStatus() + ").");
            } else if (ratingService.hasRatingForBooking(bookingId)) { // Check if already rated
                 model.addAttribute("errorMessage", "You have already rated this booking.");
            }

            RatingDto ratingDto = new RatingDto();
            ratingDto.setBookingId(bookingId);

            model.addAttribute("ratingDto", ratingDto);
            model.addAttribute("booking", booking);

        } catch (EntityNotFoundException e) {
             model.addAttribute("errorMessage", e.getMessage());
             // No booking object added if not found
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred while preparing the rating form.");
            e.printStackTrace();
        }
        return "rating-form";
    }

    @PostMapping
    public String submitRating(@PathVariable Long bookingId,
                               @Valid @ModelAttribute("ratingDto") RatingDto ratingDto,
                               BindingResult bindingResult,
                               Principal principal,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if (principal == null) return "redirect:/login";
        String customerEmail = principal.getName();

        // Re-fetch booking to ensure context and prevent manipulation
         Booking booking = bookingService.findBookingByIdAndCustomerEmail(bookingId, customerEmail)
                 .orElse(null); // Fetch booking again for context if needed

        if (booking == null) {
             redirectAttributes.addFlashAttribute("errorMessage", "Booking not found or access denied.");
             return "redirect:/bookings/my";
        }

        // Basic validation check
        if (bindingResult.hasErrors()) {
            model.addAttribute("booking", booking); // Add booking back to model for form redisplay
            // Don't add error message here, rely on field errors, but return the form view
            return "rating-form"; // Return to form view to show errors
        }

        // Ensure the DTO bookingId matches the path variable
        if (!bookingId.equals(ratingDto.getBookingId())) {
             redirectAttributes.addFlashAttribute("errorMessage", "Booking ID mismatch.");
             return "redirect:/bookings/my";
        }


        try {
            ratingService.createRating(ratingDto, customerEmail);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you for your rating!");
            return "redirect:/bookings/my"; // Redirect to bookings list on success
            // Or redirect back to the rating page with a success param:
            // return "redirect:/bookings/" + bookingId + "/rate?success=true";

        } catch (EntityNotFoundException | IllegalStateException e) {
            // Handle specific errors like "already rated" or "status not allowed" from the service
            redirectAttributes.addFlashAttribute("errorMessage", "Could not submit rating: " + e.getMessage());
             return "redirect:/bookings/" + bookingId + "/rate"; // Redirect back to form with error
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while submitting the rating.");
            e.printStackTrace();
             return "redirect:/bookings/" + bookingId + "/rate"; // Redirect back to form with error
        }
    }

     // Add a method to RatingService if needed:
     // public boolean hasRatingForBooking(Long bookingId) {
     //    return ratingRepository.existsByBooking_Id(bookingId);
     // }
}
