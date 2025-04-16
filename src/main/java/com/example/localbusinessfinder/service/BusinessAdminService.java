package com.example.localbusinessfinder.service;

import com.example.localbusinessfinder.dto.BusinessAdminDto;
import com.example.localbusinessfinder.entity.BusinessAdmin;
import com.example.localbusinessfinder.repository.BusinessAdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessAdminService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessAdminService.class);

    @Autowired
    private BusinessAdminRepository businessAdminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public BusinessAdmin registerAdmin(BusinessAdminDto adminDto) {
        if (businessAdminRepository.existsByEmail(adminDto.getEmail())) {
            logger.warn("Registration attempt failed: Email {} already exists.", adminDto.getEmail());
            throw new IllegalArgumentException("Email address already in use.");
        }
        if (!adminDto.getPassword().equals(adminDto.getConfirmPassword())) {
             logger.warn("Registration attempt failed: Passwords do not match for email {}.", adminDto.getEmail());
            throw new IllegalArgumentException("Passwords do not match.");
        }

        BusinessAdmin admin = new BusinessAdmin();
        admin.setName(adminDto.getName());
        admin.setEmail(adminDto.getEmail());
        admin.setPassword(passwordEncoder.encode(adminDto.getPassword()));
        admin.setRoles("ROLE_BUSINESS_ADMIN"); // Explicitly set role

        BusinessAdmin savedAdmin = businessAdminRepository.save(admin);
        logger.info("Successfully registered new business admin: {}", savedAdmin.getEmail());
        return savedAdmin;
    }

     public BusinessAdmin findByEmail(String email) {
        return businessAdminRepository.findByEmail(email).orElse(null);
    }
} 