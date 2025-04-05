// src/main/java/com/example/localbusinessfinder/controller/SearchController.java
package com.example.localbusinessfinder.controller;

import com.example.localbusinessfinder.entity.Business;
import com.example.localbusinessfinder.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private BusinessService businessService;

    @GetMapping({"/", "/search"}) // Handle root and /search
    public String showSearchForm(Model model) {
        // Add any default values or lists (e.g., categories) if needed
        model.addAttribute("now", LocalDateTime.now()); // For default date/time
        return "search"; // Renders search.html
    }

    @GetMapping("/results")
    public String searchResults(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam(required = false) Integer priceRange, // e.g., 1, 2, 3
            @RequestParam(required = false) Float minRating, // e.g., 4.0
            Model model) {

        List<Business> results = Collections.emptyList();
        try {
             // Basic validation/defaults
            if (dateTime == null) {
                 dateTime = LocalDateTime.now().plusHours(1); // Default to 1 hour from now if not provided
            }
             if (minRating != null && (minRating < 1 || minRating > 5)) minRating = null;
             if (priceRange != null && (priceRange < 1 || priceRange > 3)) priceRange = null;


            results = businessService.searchBusinesses(name, city, category, dateTime, priceRange, minRating);

             model.addAttribute("businesses", results);
             model.addAttribute("searchDateTime", dateTime); // Pass dateTime to results page for booking links
             model.addAttribute("searchPerformed", true);

        } catch (Exception e) {
             model.addAttribute("errorMessage", "Error during search: " + e.getMessage());
             model.addAttribute("businesses", results); // Empty list
             System.err.println("Search error: " + e.getMessage());
             e.printStackTrace();
        }


        return "results"; // Renders results.html
    }
}