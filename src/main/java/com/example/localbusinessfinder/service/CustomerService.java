// src/main/java/com/example/localbusinessfinder/service/CustomerService.java
package com.example.localbusinessfinder.service;

import com.example.localbusinessfinder.dto.CustomerDto;
import com.example.localbusinessfinder.entity.Customer;
import com.example.localbusinessfinder.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    @Transactional
    public Customer saveCustomer(CustomerDto customerDto) {
        if (existsByEmail(customerDto.getEmail())) {
            logger.warn("Attempted to save customer with existing email: {}", customerDto.getEmail());
            throw new IllegalArgumentException("Email address already in use: " + customerDto.getEmail());
        }

        Customer customer = new Customer();
        customer.setName(customerDto.getName());
        customer.setEmail(customerDto.getEmail());
        customer.setPassword(passwordEncoder.encode(customerDto.getPassword()));
        customer.setRoles("ROLE_CUSTOMER");

        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Successfully saved new customer: {}", savedCustomer.getEmail());
        return savedCustomer;
    }
}