package com.example.localbusinessfinder.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appConfig") // Bean name used in Thymeleaf
public class AppProperties {

    @Value("${app.cancellation.grace-hours:3}")
    private int cancellationGraceHours;

    // --- Getters ---
    public int getCancellationGraceHours() {
        return cancellationGraceHours;
    }

    // Add other properties and their getters as needed
} 