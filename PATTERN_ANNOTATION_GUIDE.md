# @Pattern Annotation Guide - Complete Explanation with Examples

## What is @Pattern Annotation?

The `@Pattern` annotation is a Jakarta Bean Validation (formerly Java Bean Validation) constraint that validates a string field against a **regular expression (regex)**. It ensures that the string matches a specific pattern before the data is processed.

### Key Points:
- ✅ Validates string format using regex
- ✅ Part of Jakarta Validation API (`jakarta.validation.constraints.Pattern`)
- ✅ Works with `@Valid` annotation in Spring controllers
- ✅ Returns validation errors automatically (400 Bad Request)
- ✅ Can be combined with other validation annotations

---

## Basic Syntax

```java
@Pattern(
    regexp = "your-regex-pattern",
    flags = Pattern.Flag.CASE_INSENSITIVE,  // Optional
    message = "Custom error message"         // Optional
)
private String fieldName;
```

### Parameters:
- **`regexp`** (required): The regular expression pattern to match
- **`flags`** (optional): Pattern flags like `CASE_INSENSITIVE`, `MULTILINE`, etc.
- **`message`** (optional): Custom error message when validation fails

---

## Examples in Current Project

### Example 1: Airport Code Validation (FlightSearchRequest)

```java
@Pattern(
    regexp = "^[A-Z]{3}$",
    flags = Pattern.Flag.CASE_INSENSITIVE,
    message = "Origin must be a valid 3-letter IATA airport code (e.g., JFK, LAX)"
)
private String origin;
```

**Regex Breakdown:**
- `^` - Start of string
- `[A-Z]{3}` - Exactly 3 uppercase letters
- `$` - End of string
- `CASE_INSENSITIVE` - Accepts "jfk" and converts to "JFK"

**Valid Examples:** `JFK`, `LAX`, `lax` (case-insensitive), `LHR`
**Invalid Examples:** `JK`, `JFK1`, `JFK-`, `123`

---

### Example 2: Email Validation (UserRegistrationRequest)

```java
@Pattern(
    regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
    message = "Email must be a valid email address"
)
private String email;
```

**Regex Breakdown:**
- `^[a-zA-Z0-9._%+-]+` - Local part (before @): letters, numbers, dots, underscores, %, +, -
- `@` - Literal @ symbol
- `[a-zA-Z0-9.-]+` - Domain name: letters, numbers, dots, hyphens
- `\\.` - Escaped dot (literal period)
- `[a-zA-Z]{2,}` - Top-level domain: 2+ letters
- `$` - End of string

**Valid Examples:** `user@example.com`, `john.doe@company.co.uk`
**Invalid Examples:** `invalid.email`, `@domain.com`, `user@domain`

---

### Example 3: Strong Password Validation

```java
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
    message = "Password must contain uppercase, lowercase, digit, and special character"
)
private String password;
```

**Regex Breakdown:**
- `^` - Start of string
- `(?=.*[a-z])` - **Positive lookahead**: Must contain at least one lowercase letter
- `(?=.*[A-Z])` - Must contain at least one uppercase letter
- `(?=.*\\d)` - Must contain at least one digit
- `(?=.*[@$!%*?&])` - Must contain at least one special character
- `[A-Za-z\\d@$!%*?&]{8,20}` - Only allowed characters, 8-20 length
- `$` - End of string

**Valid Examples:** `Password123!`, `MyP@ssw0rd`, `Secure#Pass1`
**Invalid Examples:** `password` (no uppercase), `PASSWORD123` (no lowercase), `Password` (no digit)

---

### Example 4: Phone Number Validation

```java
@Pattern(
    regexp = "^(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$",
    message = "Phone number must be in valid format"
)
private String phoneNumber;
```

**Regex Breakdown:**
- `^` - Start
- `(\\+?\\d{1,3}[-.\\s]?)?` - Optional country code (+1, +44)
- `\\(?\\d{3}\\)?` - Optional area code in parentheses
- `[-.\\s]?` - Optional separator (dash, dot, or space)
- `\\d{3}` - Three digits
- `[-.\\s]?` - Optional separator
- `\\d{4}` - Four digits
- `$` - End

**Valid Examples:**
- `+1-555-123-4567`
- `(555) 123-4567`
- `555.123.4567`
- `5551234567`

---

### Example 5: Credit Card Number Format

```java
@Pattern(
    regexp = "^\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}$",
    message = "Credit card must be 16 digits"
)
private String creditCardNumber;
```

**Valid Examples:**
- `1234-5678-9012-3456`
- `1234 5678 9012 3456`
- `1234567890123456`

---

## How It Works in Spring Boot

### 1. Controller with @Valid

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistrationRequest request) {
        // If validation fails, Spring automatically returns 400 Bad Request
        // with validation error messages
        return ResponseEntity.ok("User registered successfully");
    }
}
```

### 2. Automatic Validation

When a request comes in:
1. Spring validates the DTO using `@Valid`
2. If `@Pattern` fails, a `MethodArgumentNotValidException` is thrown
3. Spring returns **400 Bad Request** with error details
4. No need to manually check in controller!

### 3. Error Response Format

```json
{
  "timestamp": "2024-12-09T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "message": "Email must be a valid email address"
    },
    {
      "field": "password",
      "rejectedValue": "weak",
      "message": "Password must contain uppercase, lowercase, digit, and special character"
    }
  ]
}
```

---

## Common Regex Patterns Reference

### Alphanumeric Only
```java
@Pattern(regexp = "^[a-zA-Z0-9]+$")
// Valid: "abc123", "ABC123"
// Invalid: "abc-123", "abc_123"
```

### Letters and Spaces Only
```java
@Pattern(regexp = "^[a-zA-Z\\s]+$")
// Valid: "John Doe", "Mary Jane"
// Invalid: "John123", "John-Doe"
```

### Numbers Only
```java
@Pattern(regexp = "^\\d+$")
// Valid: "123", "456789"
// Invalid: "12.34", "12-34"
```

### Decimal Number
```java
@Pattern(regexp = "^\\d+(\\.\\d{1,2})?$")
// Valid: "123", "123.45", "0.5"
// Invalid: "123.456", "abc"
```

### Time Format (HH:MM)
```java
@Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$")
// Valid: "09:30", "23:59", "00:00"
// Invalid: "24:00", "9:5", "25:30"
```

### IP Address
```java
@Pattern(regexp = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$")
// Valid: "192.168.1.1", "10.0.0.1"
// Invalid: "256.1.1.1", "192.168.1"
```

---

## Best Practices

### ✅ DO:
1. **Use descriptive error messages**
   ```java
   @Pattern(regexp = "^[A-Z]{3}$",
            message = "Airport code must be exactly 3 uppercase letters")
   ```

2. **Combine with other validations**
   ```java
   @NotBlank
   @Size(min = 3, max = 3)
   @Pattern(regexp = "^[A-Z]{3}$")
   private String airportCode;
   ```

3. **Use CASE_INSENSITIVE flag when appropriate**
   ```java
   @Pattern(regexp = "^[A-Z]{3}$",
            flags = Pattern.Flag.CASE_INSENSITIVE)
   ```

4. **Test your regex patterns thoroughly**
   - Use online regex testers
   - Test edge cases
   - Test invalid inputs

### ❌ DON'T:
1. **Don't use overly complex regex** - Keep it readable
2. **Don't forget to escape special characters** - Use `\\.` for literal dot
3. **Don't validate sensitive data format only** - Use additional security measures
4. **Don't rely solely on client-side validation** - Always validate on server

---

## Testing Pattern Validation

### Unit Test Example

```java
@Test
void testAirportCodeValidation() {
    FlightSearchRequest request = new FlightSearchRequest();

    // Valid
    request.setOrigin("JFK");
    Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());

    // Invalid - too short
    request.setOrigin("JK");
    violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertEquals("Origin must be a valid 3-letter IATA airport code",
                 violations.iterator().next().getMessage());

    // Invalid - contains numbers
    request.setOrigin("JFK1");
    violations = validator.validate(request);
    assertFalse(violations.isEmpty());
}
```

### Integration Test Example

```java
@Test
void testFlightSearchWithInvalidOrigin() throws Exception {
    mockMvc.perform(get("/api/flights/search/stream")
            .param("origin", "JK")  // Invalid - only 2 letters
            .param("destination", "LAX")
            .param("departureDate", "2024-12-25"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("origin"))
        .andExpect(jsonPath("$.errors[0].message").value(
            "Origin must be a valid 3-letter IATA airport code"));
}
```

---

## Common Regex Symbols Reference

| Symbol | Meaning | Example |
|--------|---------|---------|
| `^` | Start of string | `^abc` matches "abc" at start |
| `$` | End of string | `abc$` matches "abc" at end |
| `.` | Any character | `a.c` matches "abc", "a1c" |
| `*` | Zero or more | `ab*` matches "a", "ab", "abb" |
| `+` | One or more | `ab+` matches "ab", "abb" |
| `?` | Zero or one | `ab?` matches "a", "ab" |
| `{n}` | Exactly n times | `a{3}` matches "aaa" |
| `{n,m}` | Between n and m times | `a{2,4}` matches "aa", "aaa", "aaaa" |
| `[abc]` | Any of a, b, or c | `[abc]` matches "a", "b", or "c" |
| `[a-z]` | Range: a to z | `[a-z]` matches any lowercase letter |
| `[^abc]` | Not a, b, or c | `[^abc]` matches "d", "1", etc. |
| `\d` | Digit (0-9) | `\d+` matches "123" |
| `\w` | Word character (a-z, A-Z, 0-9, _) | `\w+` matches "abc123" |
| `\s` | Whitespace | `\s+` matches spaces, tabs |
| `\|` | OR | `a\|b` matches "a" or "b" |
| `()` | Group | `(ab)+` matches "ab", "abab" |
| `(?=...)` | Positive lookahead | `(?=\d)` requires digit ahead |

---

## Summary

The `@Pattern` annotation is a powerful tool for:
- ✅ **Format validation** - Email, phone, credit card formats
- ✅ **Data integrity** - Ensuring data matches expected structure
- ✅ **Security** - Preventing malformed input
- ✅ **User experience** - Clear error messages for invalid input
- ✅ **API documentation** - Self-documenting validation rules

**Remember**: Always validate on the server side, even if you validate on the client. The `@Pattern` annotation makes server-side validation automatic and consistent!

---

## Quick Reference Card

```java
// Airport code (3 letters)
@Pattern(regexp = "^[A-Z]{3}$", flags = Pattern.Flag.CASE_INSENSITIVE)

// Email
@Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")

// Phone (US format)
@Pattern(regexp = "^(\\+?1[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$")

// Strong password
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$")

// Credit card (16 digits)
@Pattern(regexp = "^\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}$")

// URL
@Pattern(regexp = "^https?://([\\da-z\\.-]+)\\.[a-z]{2,}([/?#].*)?$",
         flags = Pattern.Flag.CASE_INSENSITIVE)
```

