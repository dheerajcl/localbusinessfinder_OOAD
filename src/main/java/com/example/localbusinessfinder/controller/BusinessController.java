// src/main/java/com/example/localbusinessfinder/controller/BusinessController.java
package com.example.localbusinessfinder.controller;

import com.example.localbusinessfinder.entity.Business;
import com.example.localbusinessfinder.service.BusinessService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    @GetMapping("/businesses/{businessId}")
    public String showBusinessDetails(@PathVariable Long businessId, Model model) {
        Business business = businessService.findBusinessById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));

        model.addAttribute("business", business);
        return "business-details"; // business-details.html
    }
}