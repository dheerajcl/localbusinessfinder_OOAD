// src/main/java/com/example/localbusinessfinder/repository/CustomerRepository.java
package com.example.localbusinessfinder.repository;

import com.example.localbusinessfinder.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
}