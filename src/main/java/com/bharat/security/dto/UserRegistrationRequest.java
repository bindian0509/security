package com.bharat.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Comprehensive example DTO demonstrating various @Pattern annotation usages.
 * This shows real-world validation patterns for user registration.
 */
public class UserRegistrationRequest {

    /**
     * Email validation using @Pattern (alternative to @Email annotation).
     *
     * Pattern breakdown:
     * - ^[a-zA-Z0-9._%+-]+ - Starts with alphanumeric, dots, underscores, %, +, or -
     * - @ - Literal @ symbol
     * - [a-zA-Z0-9.-]+ - Domain name (alphanumeric, dots, hyphens)
     * - \\. - Escaped dot
     * - [a-zA-Z]{2,} - Top-level domain (2+ letters)
     * - $ - End of string
     *
     * Example valid: user@example.com, john.doe@company.co.uk
     * Example invalid: invalid.email, @domain.com, user@domain
     */
    @NotBlank(message = "Email is required")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "Email must be a valid email address (e.g., user@example.com)"
    )
    private String email;

    /**
     * Phone number validation - supports multiple formats.
     *
     * Pattern breakdown:
     * - ^ - Start of string
     * - (\\+?\\d{1,3}[-.\\s]?)? - Optional country code (+1, +44, etc.)
     * - \\(?\\d{3}\\)? - Optional area code in parentheses (123) or 123
     * - [-.\\s]? - Optional separator (-, ., or space)
     * - \\d{3} - Three digits
     * - [-.\\s]? - Optional separator
     * - \\d{4} - Four digits
     * - $ - End of string
     *
     * Examples: +1-555-123-4567, (555) 123-4567, 555.123.4567, 5551234567
     */
    @Pattern(
        regexp = "^(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$",
        message = "Phone number must be in valid format (e.g., +1-555-123-4567, (555) 123-4567)"
    )
    private String phoneNumber;

    /**
     * Password validation - strong password requirements.
     *
     * Pattern breakdown:
     * - ^ - Start of string
     * - (?=.*[a-z]) - At least one lowercase letter (positive lookahead)
     * - (?=.*[A-Z]) - At least one uppercase letter
     * - (?=.*\\d) - At least one digit
     * - (?=.*[@$!%*?&]) - At least one special character
     * - [A-Za-z\\d@$!%*?&] - Only allowed characters
     * - {8,20} - Between 8 and 20 characters
     * - $ - End of string
     *
     * Examples: Password123!, MyP@ssw0rd, Secure#Pass1
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)"
    )
    private String password;

    /**
     * Credit card number validation (basic format check, not Luhn algorithm).
     *
     * Pattern breakdown:
     * - ^ - Start of string
     * - \\d{4} - Four digits
     * - [-\\s]? - Optional separator (dash or space)
     * - (repeated 3 times for 4 groups)
     * - \\d{4} - Final four digits
     * - $ - End of string
     *
     * Examples: 1234-5678-9012-3456, 1234 5678 9012 3456, 1234567890123456
     */
    @Pattern(
        regexp = "^\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}$",
        message = "Credit card must be 16 digits, optionally separated by dashes or spaces"
    )
    private String creditCardNumber;

    /**
     * ZIP/Postal code validation - US and international formats.
     *
     * Pattern breakdown:
     * - ^ - Start of string
     * - (\\d{5}(-\\d{4})? - US ZIP: 5 digits, optional -4 digits
     * - | - OR
     * - [A-Z]{1,2}\\d{1,2}[A-Z]?\\s?\\d[A-Z]{2} - UK postcode format
     * - | - OR
     * - \\d{5} - Simple 5-digit code
     * - ) - End of group
     * - $ - End of string
     *
     * Examples: 12345, 12345-6789, SW1A 1AA, 90210
     */
    @Pattern(
        regexp = "^(\\d{5}(-\\d{4})?|[A-Z]{1,2}\\d{1,2}[A-Z]?\\s?\\d[A-Z]{2}|\\d{5})$",
        flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "Postal code must be in valid format (e.g., 12345, 12345-6789, SW1A 1AA)"
    )
    private String postalCode;

    /**
     * Username validation - alphanumeric and underscores only.
     *
     * Pattern breakdown:
     * - ^[a-zA-Z0-9_]+$ - Only letters, numbers, and underscores
     * - {3,20} - Between 3 and 20 characters
     *
     * Examples: john_doe, user123, admin
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "Username can only contain letters, numbers, and underscores"
    )
    private String username;

    /**
     * Flight number validation - airline code + digits.
     *
     * Pattern breakdown:
     * - ^[A-Z]{2} - Two uppercase letters (airline code)
     * - \\d{1,4} - One to four digits (flight number)
     * - $ - End of string
     *
     * Examples: AA123, DL4567, UA89
     */
    @Pattern(
        regexp = "^[A-Z]{2}\\d{1,4}$",
        flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "Flight number must be in format: 2 letters followed by 1-4 digits (e.g., AA123, DL4567)"
    )
    private String flightNumber;

    /**
     * Date format validation - YYYY-MM-DD.
     *
     * Pattern breakdown:
     * - ^\\d{4} - Four digits (year)
     * - - - Literal dash
     * - \\d{2} - Two digits (month)
     * - - - Literal dash
     * - \\d{2} - Two digits (day)
     * - $ - End of string
     *
     * Example: 2024-12-25
     */
    @Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2}$",
        message = "Date must be in YYYY-MM-DD format (e.g., 2024-12-25)"
    )
    private String dateString;

    /**
     * URL validation - basic URL format check.
     *
     * Pattern breakdown:
     * - ^https?:// - Starts with http:// or https://
     * - ([\\da-z\\.-]+)\\. - Domain name (alphanumeric, dots, hyphens)
     * - [a-z]{2,} - Top-level domain (2+ letters)
     * - ([/?#].*)? - Optional path, query, or fragment
     * - $ - End of string
     *
     * Examples: https://example.com, http://subdomain.example.com/path?query=value
     */
    @Pattern(
        regexp = "^https?://([\\da-z\\.-]+)\\.[a-z]{2,}([/?#].*)?$",
        flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "URL must be a valid HTTP/HTTPS URL"
    )
    private String websiteUrl;

    // Constructors
    public UserRegistrationRequest() {
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
}

