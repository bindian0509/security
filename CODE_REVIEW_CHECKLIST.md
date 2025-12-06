# Code Review Checklist for Java Spring REST API Projects

## 🔒 Security

### Authentication & Authorization
- [ ] **No hardcoded credentials** - Check for passwords, API keys, tokens in code
- [ ] **Password encoding** - All passwords must use proper encoders (BCrypt, Argon2, etc.)
- [ ] **Authentication required** - Verify endpoints are properly secured
- [ ] **Role-based access control** - Check `@PreAuthorize`, `@Secured`, or method-level security
- [ ] **CSRF protection** - Enabled for state-changing operations (POST, PUT, DELETE)
- [ ] **CORS configuration** - Properly configured, not `allowAll()` in production
- [ ] **Input validation** - All user inputs validated using `@Valid`, `@NotNull`, `@Size`, etc.
- [ ] **SQL Injection prevention** - Using parameterized queries, JPA repositories (not string concatenation)
- [ ] **XSS prevention** - Output encoding, no raw HTML in responses
- [ ] **Sensitive data exposure** - No passwords, tokens, or PII in logs
- [ ] **Security headers** - Check for proper HTTP security headers (HSTS, X-Frame-Options, etc.)

### Example Issues to Flag:
```java
// ❌ BAD - Hardcoded password
UserDetails user = User.withUsername("admin").password("admin123").build();

// ✅ GOOD - Encoded password
UserDetails user = User.withUsername("admin")
    .password("{bcrypt}$2a$12$...").build();

// ❌ BAD - No validation
@GetMapping("/user/{id}")
public User getUser(@PathVariable String id) { ... }

// ✅ GOOD - Validated input
@GetMapping("/user/{id}")
public User getUser(@PathVariable @NotNull @Min(1) Long id) { ... }
```

---

## 🏗️ Code Quality & Best Practices

### General Java Practices
- [ ] **Null safety** - Proper null checks, use `Optional` where appropriate
- [ ] **Exception handling** - No empty catch blocks, proper exception types
- [ ] **Resource management** - Try-with-resources for streams, connections
- [ ] **Immutability** - Prefer immutable objects, use `final` for fields
- [ ] **Single Responsibility** - Classes/methods have one clear purpose
- [ ] **DRY principle** - No code duplication
- [ ] **Magic numbers/strings** - Use constants or enums
- [ ] **Naming conventions** - Clear, descriptive names following Java conventions

### Spring-Specific
- [ ] **Dependency Injection** - Constructor injection preferred over field injection
- [ ] **Bean scope** - Appropriate scope (`@Singleton`, `@Request`, etc.)
- [ ] **Circular dependencies** - None present
- [ ] **Transaction management** - Proper `@Transactional` usage
- [ ] **Lazy initialization** - Used appropriately for performance

### Example Issues:
```java
// ❌ BAD - Field injection
@Autowired
private AccountService accountService;

// ✅ GOOD - Constructor injection
private final AccountService accountService;
public AccountController(AccountService accountService) {
    this.accountService = accountService;
}

// ❌ BAD - Empty catch block
try {
    riskyOperation();
} catch (Exception e) {
    // ignored
}

// ✅ GOOD - Proper exception handling
try {
    riskyOperation();
} catch (SpecificException e) {
    logger.error("Operation failed", e);
    throw new ServiceException("Operation failed", e);
}
```

---

## 🌐 REST API Design

### HTTP Standards
- [ ] **HTTP methods** - Correct usage (GET for read, POST for create, PUT for update, DELETE for delete)
- [ ] **Status codes** - Appropriate HTTP status codes (200, 201, 204, 400, 401, 403, 404, 500)
- [ ] **Idempotency** - PUT and DELETE operations are idempotent
- [ ] **URL design** - RESTful, hierarchical, no verbs in URLs
- [ ] **Content negotiation** - Proper `Accept` and `Content-Type` headers
- [ ] **Pagination** - Large collections use pagination
- [ ] **Versioning** - API versioning strategy (URL, header, or content negotiation)

### Request/Response
- [ ] **DTOs used** - Separate DTOs from entities, no entity exposure
- [ ] **Validation annotations** - Request DTOs properly validated
- [ ] **Response consistency** - Consistent response structure
- [ ] **Error responses** - Standardized error response format
- [ ] **HATEOAS** - Consider hypermedia links where appropriate

### Example Issues:
```java
// ❌ BAD - Entity exposed directly
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).orElseThrow();
}

// ✅ GOOD - DTO used
@GetMapping("/users/{id}")
public UserResponseDTO getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return userMapper.toDTO(user);
}

// ❌ BAD - Wrong HTTP method
@PostMapping("/users/{id}/delete")
public void deleteUser(@PathVariable Long id) { ... }

// ✅ GOOD - Correct HTTP method
@DeleteMapping("/users/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) { ... }
```

---

## ⚠️ Error Handling

- [ ] **Global exception handler** - `@ControllerAdvice` or `@RestControllerAdvice` present
- [ ] **Custom exceptions** - Domain-specific exceptions, not generic `RuntimeException`
- [ ] **Error messages** - User-friendly messages, no stack traces in production
- [ ] **Logging** - Exceptions logged with context (request ID, user, etc.)
- [ ] **Error response format** - Consistent error response structure
- [ ] **HTTP status mapping** - Exceptions mapped to appropriate HTTP status codes

### Example:
```java
// ✅ GOOD - Global exception handler
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        logger.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("VALIDATION_ERROR", ex.getMessage()));
    }
}
```

---

## 🧪 Testing

- [ ] **Unit tests** - Service layer has unit tests
- [ ] **Integration tests** - Controller endpoints tested
- [ ] **Test coverage** - Reasonable coverage (aim for >80% for critical paths)
- [ ] **Test data** - No hardcoded test data, use builders/factories
- [ ] **Mocking** - Proper use of mocks, avoid over-mocking
- [ ] **Test isolation** - Tests don't depend on each other
- [ ] **Edge cases** - Boundary conditions, null values, empty collections tested

### Example:
```java
// ✅ GOOD - Comprehensive test
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Test
    void getAccountDetails_WhenAccountExists_ReturnsAccount() {
        // Given
        String accountId = "12345";

        // When
        mockMvc.perform(get("/account/{accountId}", accountId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(accountId));
    }

    @Test
    void getAccountDetails_WhenAccountNotFound_Returns404() {
        // Given
        String accountId = "99999";

        // When & Then
        mockMvc.perform(get("/account/{accountId}", accountId))
            .andExpect(status().isNotFound());
    }
}
```

---

## 📊 Logging

- [ ] **Logging framework** - SLF4J with Logback/Log4j2 (not System.out.println)
- [ ] **Log levels** - Appropriate levels (DEBUG, INFO, WARN, ERROR)
- [ ] **Structured logging** - Parameterized logging (`logger.info("User {} created", userId)`)
- [ ] **Sensitive data** - No passwords, tokens, PII in logs
- [ ] **Context information** - Request IDs, user IDs, correlation IDs
- [ ] **Performance logging** - Critical operations logged with timing
- [ ] **Error logging** - Exceptions logged with full stack traces

### Example:
```java
// ❌ BAD - String concatenation, sensitive data
logger.info("User " + username + " logged in with password " + password);

// ✅ GOOD - Parameterized, no sensitive data
logger.info("User {} logged in successfully", username);

// ✅ GOOD - Error with context
logger.error("Failed to process order {} for user {}", orderId, userId, exception);
```

---

## ⚙️ Configuration

- [ ] **Externalized configuration** - No hardcoded values, use `application.properties`/`application.yml`
- [ ] **Environment-specific** - Different configs for dev/staging/prod
- [ ] **Sensitive data** - Secrets in environment variables or secret management
- [ ] **Configuration validation** - `@ConfigurationProperties` with validation
- [ ] **Feature flags** - Use for toggling features
- [ ] **Database configuration** - Connection pooling, transaction timeout configured

### Example:
```java
// ✅ GOOD - Configuration properties
@ConfigurationProperties(prefix = "app.payment")
@Validated
public class PaymentProperties {
    @NotNull
    private String apiKey;

    @Min(1)
    @Max(60)
    private int timeoutSeconds = 30;

    // getters/setters
}
```

---

## 🚀 Performance

- [ ] **N+1 queries** - Check for lazy loading issues, use `@EntityGraph` or fetch joins
- [ ] **Database indexes** - Appropriate indexes on frequently queried columns
- [ ] **Caching** - Appropriate use of `@Cacheable` for expensive operations
- [ ] **Pagination** - Large datasets paginated
- [ ] **Async processing** - Long-running tasks use `@Async` or message queues
- [ ] **Connection pooling** - Database connection pool configured
- [ ] **Response compression** - Enabled for large responses
- [ ] **Rate limiting** - Consider for public APIs

### Example:
```java
// ❌ BAD - N+1 query problem
@GetMapping("/users")
public List<UserDTO> getUsers() {
    List<User> users = userRepository.findAll();
    // This will cause N+1 queries
    return users.stream()
        .map(user -> new UserDTO(user, user.getOrders())) // Lazy loading
        .collect(Collectors.toList());
}

// ✅ GOOD - Fetch join
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders")
List<User> findAllWithOrders();
```

---

## 📝 Documentation

- [ ] **JavaDoc** - Public APIs documented
- [ ] **API documentation** - OpenAPI/Swagger annotations present
- [ ] **README** - Updated with new features/changes
- [ ] **Code comments** - Complex logic explained
- [ ] **CHANGELOG** - Breaking changes documented

### Example:
```java
// ✅ GOOD - API documentation
@Operation(summary = "Get account details", description = "Retrieves account information by ID")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Account found"),
    @ApiResponse(responseCode = "404", description = "Account not found"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
})
@GetMapping("/{accountId}")
public ResponseEntity<AccountDTO> getAccount(@Parameter(description = "Account ID") @PathVariable Long accountId) {
    // implementation
}
```

---

## 🔍 Code Review Red Flags

### Critical Issues (Block PR)
- Hardcoded credentials or secrets
- SQL injection vulnerabilities
- Missing authentication/authorization
- Empty catch blocks swallowing exceptions
- No input validation
- Exposing internal exceptions to clients
- Missing error handling

### Major Issues (Should Fix)
- Code duplication
- Poor exception handling
- Missing tests for critical paths
- Performance issues (N+1 queries, etc.)
- Security misconfigurations
- Inconsistent error responses

### Minor Issues (Nice to Have)
- Code style inconsistencies
- Missing JavaDoc
- Magic numbers/strings
- Verbose code that could be simplified

---

## 📋 Quick Review Checklist

### Before Approving PR:
1. ✅ Code compiles and tests pass
2. ✅ No security vulnerabilities
3. ✅ Proper error handling
4. ✅ Adequate test coverage
5. ✅ Follows REST API conventions
6. ✅ No sensitive data in logs/code
7. ✅ Configuration externalized
8. ✅ Documentation updated
9. ✅ Performance considerations addressed
10. ✅ Code follows project conventions

---

## 🛠️ Tools to Use

- **Static Analysis**: SonarQube, SpotBugs, PMD
- **Security Scanning**: OWASP Dependency Check, Snyk
- **Code Coverage**: JaCoCo, Cobertura
- **API Testing**: Postman, REST Assured
- **Performance**: JMeter, Gatling
- **Documentation**: Swagger/OpenAPI

---

## 📚 Additional Resources

- [Spring Security Best Practices](https://spring.io/guides/topicals/spring-security-architecture)
- [REST API Design Guidelines](https://restfulapi.net/)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)

---

**Remember**: Code review is not just about finding bugs—it's about improving code quality, sharing knowledge, and maintaining consistency across the codebase.

