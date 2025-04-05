// src/main/java/com/example/localbusinessfinder/controller/AuthController.java
package com.example.localbusinessfinder.controller;

import com.example.localbusinessfinder.dto.CustomerRegistrationDto;
import com.example.localbusinessfinder.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/login")
    public String loginForm() {
        return "login"; // Renders login.html
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("customerDto", new CustomerRegistrationDto());
        return "register"; // Renders register.html
    }

    @PostMapping("/register")
    public String registerCustomer(@Valid @ModelAttribute("customerDto") CustomerRegistrationDto customerDto,
                                   BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        // Custom validation for password match and existing email
        if (!customerDto.getPassword().equals(customerDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.customerDto", "Passwords do not match");
        }
        if (customerService.emailExists(customerDto.getEmail())) {
             result.rejectValue("email", "error.customerDto", "An account already exists for this email");
        }


        if (result.hasErrors()) {
            model.addAttribute("customerDto", customerDto); // Keep data in form
            return "register";
        }

        try {
            customerService.registerCustomer(customerDto);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("customerDto", customerDto);
            return "register";
        }
    }
}
