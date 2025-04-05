// src/main/java/com/example/localbusinessfinder/enums/BookingStatus.java
package com.example.localbusinessfinder.enums;

public enum BookingStatus {
    CONFIRMED, // Initial state after advance payment
    CANCELLED_REFUNDED, // Cancelled within grace period
    CANCELLED_PENALTY,  // Cancelled after grace period
    AWAITING_FINAL_PAYMENT, // Adjusted price set, waiting for final payment
    FULLY_PAID, // Final payment done
    COMPLETED_PENDING_RATING // Service done, rating possible (optional distinct state)
}