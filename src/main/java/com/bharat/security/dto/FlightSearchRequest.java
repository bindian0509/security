package com.bharat.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * Request DTO for flight search.
 */
public class FlightSearchRequest {

    /**
     * Origin airport code - must be exactly 3 uppercase letters (IATA format).
     * Example: JFK, LAX, LHR
     *
     * @Pattern regex explanation:
     * - ^[A-Z]{3}$ - Starts (^) and ends ($) with exactly 3 uppercase letters [A-Z]
     * - flags = Pattern.Flag.CASE_INSENSITIVE - Allows lowercase input, converts to uppercase
     */
    @NotBlank(message = "Origin is required")
    @Pattern(
        regexp = "^[A-Z]{3}$",
        flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "Origin must be a valid 3-letter IATA airport code (e.g., JFK, LAX)"
    )
    private String origin;

    /**
     * Destination airport code - must be exactly 3 uppercase letters (IATA format).
     * Example: JFK, LAX, LHR
     */
    @NotBlank(message = "Destination is required")
    @Pattern(
        regexp = "^[A-Z]{3}$",
        flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "Destination must be a valid 3-letter IATA airport code (e.g., JFK, LAX)"
    )
    private String destination;

    @NotNull(message = "Departure date is required")
    private LocalDate departureDate;

    private LocalDate returnDate;

    private Integer passengers = 1;

    // Constructors
    public FlightSearchRequest() {
    }

    public FlightSearchRequest(String origin, String destination, LocalDate departureDate) {
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
    }

    // Getters and Setters
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Integer getPassengers() {
        return passengers;
    }

    public void setPassengers(Integer passengers) {
        this.passengers = passengers;
    }
}

