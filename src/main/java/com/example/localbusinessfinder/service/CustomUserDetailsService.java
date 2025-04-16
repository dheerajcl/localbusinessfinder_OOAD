package com.example.localbusinessfinder.service;

import com.example.localbusinessfinder.entity.BusinessAdmin;
import com.example.localbusinessfinder.entity.Customer;
import com.example.localbusinessfinder.repository.BusinessAdminRepository;
import com.example.localbusinessfinder.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BusinessAdminRepository businessAdminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try loading as Customer first
        Customer customer = customerRepository.findByEmail(username).orElse(null);
        if (customer != null) {
            return new User(customer.getEmail(),
                    customer.getPassword(),
                    mapRolesToAuthorities(customer.getRoles()));
        }

        // If not found as Customer, try loading as BusinessAdmin
        BusinessAdmin admin = businessAdminRepository.findByEmail(username).orElse(null);
        if (admin != null) {
             return new User(admin.getEmail(),
                    admin.getPassword(),
                    mapRolesToAuthorities(admin.getRoles()));
        }

        throw new UsernameNotFoundException("Invalid username or password.");
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(String roles) {
         // Roles are stored as comma-separated string, e.g., "ROLE_USER,ROLE_ADMIN"
         // Or just a single role like "ROLE_BUSINESS_ADMIN" or "ROLE_CUSTOMER"
        return Arrays.stream(roles.split(","))
                .map(String::trim) // Remove whitespace
                .filter(role -> !role.isEmpty()) // Ensure no empty strings
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
} 