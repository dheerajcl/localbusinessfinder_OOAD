# Local Business Finder

A web application built with Java and Spring Boot that allows users to find, book, and rate local services. This project aims to replicate core functionalities similar to platforms like Urban Company, focusing on a demo-ready implementation with simulated payments.

## Features

*   **User Authentication:** Secure registration and login for customers.
*   **Service Search:** Users can search for businesses/services.
*   **Business Details:** View details about a specific business.
*   **Booking System:** Customers can book services through a form.
*   **Booking Management:** Customers can view their past and upcoming bookings (`my-bookings`).
*   **Booking Details:** View specific details of a booking, including status.
*   **Simulated Payments:** A simplified payment flow (advance and final) is simulated to update booking status.
*   **Rating System:** Customers can rate completed services.

## Tech Stack

*   **Backend:** Java, Spring Boot, Spring MVC, Spring Security, Spring Data JPA (Hibernate)
*   **Frontend:** Thymeleaf, HTML, CSS (likely Bootstrap based on class names seen)
*   **Database:** H2 (In-Memory, common for demos) / PostgreSQL (or other relational DB for production)
*   **Build Tool:** Maven

## Setup and Running

1.  **Prerequisites:**
    *   JDK (Java Development Kit) installed (Version 17 or compatible is recommended for modern Spring Boot).
    *   Apache Maven installed.
2.  **Clone the repository (if applicable):**
    ```bash
    git clone <your-repository-url>
    cd local-business-finder
    ```
3.  **Run the application:**
    Open a terminal in the project's root directory (where `pom.xml` is located) and run:
    ```bash
    mvn spring-boot:run
    ```
4.  **Access the application:**
    Open your web browser and navigate to `http://localhost:8080` (or the configured port).

## Project Structure

*   `src/main/java/com/example/localbusinessfinder`
    *   `config`: Spring configurations (e.g., Security).
    *   `controller`: Handles HTTP requests and responses.
    *   `dto`: Data Transfer Objects used between layers.
    *   `entity`: JPA entities mapping to database tables.
    *   `enums`: Enumerations (e.g., `BookingStatus`).
    *   `repository`: Spring Data JPA interfaces for database operations.
    *   `service`: Contains the core business logic.
*   `src/main/resources`
    *   `application.properties`: Application configuration (database, server port, etc.).
    *   `static`: Static assets (CSS, JS, Images).
    *   `templates`: Thymeleaf HTML templates for the UI.

## Future Enhancements (Potential Ideas)

*   Business/Service Provider Portal
*   Customer Profile Management
*   Enhanced Booking Flow (Scheduling, Cancellation)
*   More detailed Payment Simulation/Invoice View
*   Admin Panel
