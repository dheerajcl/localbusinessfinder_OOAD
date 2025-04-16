// src/main/java/com/example/localbusinessfinder/controller/AuthController.java
package com.example.localbusinessfinder.controller;

import com.example.localbusinessfinder.dto.CustomerDto;
import com.example.localbusinessfinder.entity.Customer;
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
        CustomerDto customer = new CustomerDto();
        model.addAttribute("customer", customer);
        return "register"; // Renders register.html
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("customer") CustomerDto customerDto,
                               BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (customerService.existsByEmail(customerDto.getEmail())) {
            result.rejectValue("email", null, "There is already an account registered with that email");
        }
        if (result.hasErrors()) {
            model.addAttribute("customer", customerDto);
            return "register";
        }

        try {
            customerService.saveCustomer(customerDto);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");
            return "redirect:/login"; // Redirect to login after successful registration
        } catch (IllegalArgumentException e) {
             model.addAttribute("errorMessage", e.getMessage());
             model.addAttribute("customer", customerDto); // Add DTO back to model
             return "register";
        }
    }

    @GetMapping("/admin/login")
    public String adminLoginForm() {
        return "admin/login"; // templates/admin/login.html
    }
}
