// src/main/java/com/example/localbusinessfinder/service/CustomerService.java
package com.example.localbusinessfinder.service;

import com.example.localbusinessfinder.dto.CustomerRegistrationDto;
import com.example.localbusinessfinder.entity.Customer;
import com.example.localbusinessfinder.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean emailExists(String email) {
        return customerRepository.existsByEmail(email);
    }

    public Customer registerCustomer(CustomerRegistrationDto registrationDto) {
        if (emailExists(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + registrationDto.getEmail());
        }
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        Customer customer = new Customer();
        customer.setName(registrationDto.getName());
        customer.setEmail(registrationDto.getEmail());
        customer.setPassword(passwordEncoder.encode(registrationDto.getPassword())); // Hash password

        return customerRepository.save(customer);
    }

     public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }
}