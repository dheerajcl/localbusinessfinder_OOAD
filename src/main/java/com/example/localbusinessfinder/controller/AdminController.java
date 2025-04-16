package com.example.localbusinessfinder.controller;

import com.example.localbusinessfinder.dto.BusinessAdminDto;
import com.example.localbusinessfinder.dto.BusinessDto;
import com.example.localbusinessfinder.entity.Business;
import com.example.localbusinessfinder.service.BusinessAdminService;
import com.example.localbusinessfinder.service.BusinessService;
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
import java.util.Optional;

@Controller
@RequestMapping("/admin") // Base path for all admin actions
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private BusinessAdminService businessAdminService;

    @Autowired
    private BusinessService businessService;

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
            model.addAttribute("business", businessOpt.get());
             model.addAttribute("businessDto", businessService.convertEntityToDto(businessOpt.get())); // For potential edit form display
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
} 