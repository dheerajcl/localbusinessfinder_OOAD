package com.example.localbusinessfinder.repository;

import com.example.localbusinessfinder.entity.BusinessAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessAdminRepository extends JpaRepository<BusinessAdmin, Long> {
    Optional<BusinessAdmin> findByEmail(String email);
    boolean existsByEmail(String email);
} 