package com.example.localbusinessfinder.controller;

import com.example.localbusinessfinder.dto.BusinessAdminDto;
import com.example.localbusinessfinder.dto.BusinessDto;
import com.example.localbusinessfinder.entity.Booking;
import com.example.localbusinessfinder.entity.Business;
import com.example.localbusinessfinder.entity.Rating;
import com.example.localbusinessfinder.service.BookingService;
import com.example.localbusinessfinder.service.BusinessAdminService;
import com.example.localbusinessfinder.service.BusinessService;
import com.example.localbusinessfinder.service.RatingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.persistence.EntityNotFoundException;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Controller
@RequestMapping("/admin") // Base path for all admin actions
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private BusinessAdminService businessAdminService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RatingService ratingService;

    // --- Registration ---

    @GetMapping("/register")
    public String showAdminRegistrationForm(Model model) {
        model.addAttribute("adminDto", new BusinessAdminDto());
        return "admin/register"; // templates/admin/register.html
    }

    @PostMapping("/register")
    public String registerAdminAccount(@Valid @ModelAttribute("adminDto") BusinessAdminDto adminDto,
                                       BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            logger.warn("Admin registration form has validation errors.");
            return "admin/register";
        }
        try {
            businessAdminService.registerAdmin(adminDto);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");
            logger.info("Admin registration successful for email: {}", adminDto.getEmail());
            return "redirect:/login"; // Redirect to common login page
        } catch (IllegalArgumentException e) {
            logger.warn("Admin registration failed for email {}: {}", adminDto.getEmail(), e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/register";
        } catch (Exception e) {
             logger.error("Unexpected error during admin registration for email {}: {}", adminDto.getEmail(), e.getMessage(), e);
             model.addAttribute("errorMessage", "An unexpected error occurred during registration.");
             return "admin/register";
        }
    }

    // --- Dashboard ---

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        String adminEmail = principal.getName();

        Optional<Business> businessOpt = businessService.findBusinessByAdminEmail(adminEmail);
        if (businessOpt.isPresent()) {
            Business business = businessOpt.get();
            model.addAttribute("business", business);
            model.addAttribute("businessDto", businessService.convertEntityToDto(business)); // For potential edit form display
            
            // Fetch active and completed bookings
            try {
                List<Booking> activeBookings = bookingService.findActiveBookingsByBusinessAdminEmail(adminEmail);
                List<Booking> completedBookings = bookingService.findCompletedBookingsByBusinessAdminEmail(adminEmail);
                
                model.addAttribute("activeBookings", activeBookings);
                model.addAttribute("completedBookings", completedBookings);
                model.addAttribute("now", LocalDateTime.now()); // For displaying current time
                
                logger.info("Loaded {} active bookings and {} completed bookings for business admin: {}", 
                    activeBookings.size(), completedBookings.size(), adminEmail);
                    
                // Fetch ratings for the business
                List<Rating> ratings = ratingService.getBusinessRatings(business.getId());
                model.addAttribute("ratings", ratings);
                logger.info("Loaded {} ratings for business admin: {}", ratings.size(), adminEmail);
                
            } catch (Exception e) {
                logger.error("Error fetching data for admin {}: {}", adminEmail, e.getMessage(), e);
                model.addAttribute("bookingErrorMessage", "Could not load data: " + e.getMessage());
            }
        } else {
            model.addAttribute("message", "You haven't added your business listing yet.");
            // Optionally provide DTO for add form if dashboard includes adding
            // model.addAttribute("businessDto", new BusinessDto());
        }
        model.addAttribute("adminEmail", adminEmail); // Pass admin email if needed
        return "admin/dashboard"; // templates/admin/dashboard.html
    }

    // --- Business Management ---

    @GetMapping("/business/new")
    public String showAddBusinessForm(Model model, Principal principal, RedirectAttributes redirectAttributes) {
         if (principal == null) return "redirect:/login";
         // Check if admin already has a business
         if (businessService.findBusinessByAdminEmail(principal.getName()).isPresent()) {
             redirectAttributes.addFlashAttribute("infoMessage", "You already have a business listing. You can edit it from the dashboard.");
             return "redirect:/admin/dashboard";
         }
        model.addAttribute("businessDto", new BusinessDto());
        model.addAttribute("pageTitle", "Add Your Business");
        return "admin/business-form"; // templates/admin/business-form.html
    }

    @PostMapping("/business")
    public String addBusiness(@Valid @ModelAttribute("businessDto") BusinessDto businessDto,
                              BindingResult result, Principal principal, RedirectAttributes redirectAttributes, Model model) {
        if (principal == null) return "redirect:/login";
        if (result.hasErrors()) {
             logger.warn("Add business form has validation errors for admin {}", principal.getName());
             model.addAttribute("pageTitle", "Add Your Business"); // Reset title for redisplay
             return "admin/business-form";
        }
        try {
            businessService.createBusiness(businessDto, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Business listing added successfully!");
            return "redirect:/admin/dashboard";
        } catch (IllegalStateException | EntityNotFoundException e) {
             logger.warn("Failed to add business for admin {}: {}", principal.getName(), e.getMessage());
             redirectAttributes.addFlashAttribute("errorMessage", "Could not add business: " + e.getMessage());
             return "redirect:/admin/business/new"; // Redirect back to form
        } catch (Exception e) {
             logger.error("Unexpected error adding business for admin {}: {}", principal.getName(), e.getMessage(), e);
             model.addAttribute("errorMessage", "An unexpected error occurred while adding the business.");
             model.addAttribute("pageTitle", "Add Your Business");
             return "admin/business-form";
        }
    }


    @GetMapping("/business/edit")
    public String showEditBusinessForm(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        String adminEmail = principal.getName();

        Optional<Business> businessOpt = businessService.findBusinessByAdminEmail(adminEmail);
        if (businessOpt.isPresent()) {
             BusinessDto dto = businessService.convertEntityToDto(businessOpt.get());
             model.addAttribute("businessDto", dto);
             model.addAttribute("pageTitle", "Edit Your Business");
             return "admin/business-form"; // Re-use the form template
        } else {
             redirectAttributes.addFlashAttribute("errorMessage", "No business listing found to edit.");
             return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/business/update")
    public String updateBusiness(@Valid @ModelAttribute("businessDto") BusinessDto businessDto,
                                 BindingResult result, Principal principal, RedirectAttributes redirectAttributes, Model model) {
         if (principal == null) return "redirect:/login";
         if (result.hasErrors()) {
             logger.warn("Edit business form has validation errors for admin {}", principal.getName());
             model.addAttribute("pageTitle", "Edit Your Business"); // Reset title for redisplay
             return "admin/business-form";
         }
        try {
            businessService.updateBusiness(businessDto, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Business listing updated successfully!");
            return "redirect:/admin/dashboard";
        } catch (IllegalStateException | EntityNotFoundException | IllegalArgumentException e) {
             logger.warn("Failed to update business for admin {}: {}", principal.getName(), e.getMessage());
             redirectAttributes.addFlashAttribute("errorMessage", "Could not update business: " + e.getMessage());
             return "redirect:/admin/business/edit"; // Redirect back to edit form
        } catch (Exception e) {
             logger.error("Unexpected error updating business for admin {}: {}", principal.getName(), e.getMessage(), e);
             model.addAttribute("errorMessage", "An unexpected error occurred while updating the business.");
              model.addAttribute("pageTitle", "Edit Your Business");
             return "admin/business-form";
        }
    }

    // Add a booking detail view endpoint
    @GetMapping("/bookings/{bookingId}")
    public String viewBookingDetails(@PathVariable Long bookingId, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        String adminEmail = principal.getName();
        
        logger.info("Admin {} attempting to view booking {}", adminEmail, bookingId);
        
        try {
            // Get the business first to verify ownership
            Optional<Business> businessOpt = businessService.findBusinessByAdminEmail(adminEmail);
            if (businessOpt.isEmpty()) {
                logger.warn("No business found for admin {}", adminEmail);
                redirectAttributes.addFlashAttribute("errorMessage", "No business found for your account.");
                return "redirect:/admin/dashboard";
            }
            
            Business business = businessOpt.get();
            logger.debug("Found business ID {} for admin {}", business.getId(), adminEmail);
            
            // Get the booking
            Optional<Booking> bookingOpt = bookingService.findBookingById(bookingId);
            if (bookingOpt.isEmpty()) {
                logger.warn("Booking ID {} not found for admin {}", bookingId, adminEmail);
                redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
                return "redirect:/admin/dashboard";
            }
            
            Booking booking = bookingOpt.get();
            
            // Verify that the booking belongs to this business
            if (booking.getBusiness() == null || business.getId() == null || 
                !booking.getBusiness().getId().equals(business.getId())) {
                logger.warn("Access denied: Booking {} does not belong to business {} of admin {}", 
                           bookingId, business.getId(), adminEmail);
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied: This booking does not belong to your business.");
                return "redirect:/admin/dashboard";
            }
            
            logger.debug("Found booking ID {} for business ID {}", booking.getId(), business.getId());
            
            // Ensure customer is loaded properly
            if (booking.getCustomer() == null) {
                logger.warn("Booking {} has no associated customer", bookingId);
                // Continue without customer data - template will handle it
            }
            
            model.addAttribute("booking", booking);
            model.addAttribute("business", business);
            model.addAttribute("now", LocalDateTime.now());
            
            return "admin/booking-detail"; // admin/booking-detail.html
        } catch (Exception e) {
            logger.error("Error viewing booking details for admin {} and booking {}: {}", 
                        adminEmail, bookingId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading booking details: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    // Add method to set adjusted price for a booking
    @PostMapping("/bookings/{bookingId}/set-price")
    public String setAdjustedPrice(@PathVariable Long bookingId,
                                  @RequestParam("adjustedPrice") BigDecimal adjustedPrice,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        String adminEmail = principal.getName();
        
        try {
            // Get the business first to verify ownership
            Optional<Business> businessOpt = businessService.findBusinessByAdminEmail(adminEmail);
            if (businessOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "No business found for your account.");
                return "redirect:/admin/dashboard";
            }
            
            Business business = businessOpt.get();
            
            // Get the booking
            Optional<Booking> bookingOpt = bookingService.findBookingById(bookingId);
            if (bookingOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
                return "redirect:/admin/dashboard";
            }
            
            Booking booking = bookingOpt.get();
            
            // Verify that the booking belongs to this business
            if (!booking.getBusiness().getId().equals(business.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied: This booking does not belong to your business.");
                return "redirect:/admin/dashboard";
            }
            
            // Set adjusted price
            bookingService.setAdjustedPrice(bookingId, adjustedPrice);
            
            redirectAttributes.addFlashAttribute("successMessage", "Final price set successfully. Waiting for customer payment.");
            return "redirect:/admin/bookings/" + bookingId;
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to set adjusted price for booking {}: {}", bookingId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid price: " + e.getMessage());
            return "redirect:/admin/bookings/" + bookingId;
        } catch (Exception e) {
            logger.error("Error setting adjusted price for booking {}: {}", bookingId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error setting price: " + e.getMessage());
            return "redirect:/admin/bookings/" + bookingId;
        }
    }
} 