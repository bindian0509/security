# OWASP Top 10 2025 - Complete Guide for Java/Spring Projects

## What is OWASP Top 10?

The **OWASP Top 10** is a standard awareness document for developers and web application security. It represents a broad consensus about the most critical security risks to web applications. The **2025 edition** reflects the evolving threat landscape with new categories and updated priorities.

---

## 🥇 1. Broken Access Control (A01:2025)

### What It Is:
Access control enforces policies so users cannot act outside their intended permissions. Broken access control occurs when these policies are not properly enforced.

### Common Examples:
- **Insecure Direct Object References (IDOR)**: Accessing resources directly via ID
- **Missing Function Level Access Control**: Not checking permissions before executing functions
- **Privilege Escalation**: Regular users accessing admin functions
- **CORS Misconfiguration**: Allowing unauthorized domains

### Java/Spring Impact: 🔴 **CRITICAL**

### Vulnerable Code Examples:

```java
// ❌ BAD - No access control check
@GetMapping("/account/{accountId}")
public Account getAccount(@PathVariable Long accountId) {
    return accountRepository.findById(accountId).orElseThrow();
    // Any user can access any account!
}

// ❌ BAD - Missing authorization check
@DeleteMapping("/admin/users/{userId}")
public void deleteUser(@PathVariable Long userId) {
    userService.delete(userId);
    // No check if user has ADMIN role!
}
```

### ✅ Secure Implementation:

```java
// ✅ GOOD - Method-level security
@GetMapping("/account/{accountId}")
@PreAuthorize("hasAuthority('read') and @accountService.isOwner(authentication.name, #accountId)")
public Account getAccount(@PathVariable Long accountId) {
    return accountService.getAccount(accountId);
}

// ✅ GOOD - Role-based access control
@DeleteMapping("/admin/users/{userId}")
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(@PathVariable Long userId) {
    userService.delete(userId);
}

// ✅ GOOD - Service-level authorization check
@Service
public class AccountService {
    public Account getAccount(Long accountId, String username) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));

        // Verify ownership
        if (!account.getOwner().equals(username) && !hasAdminRole(username)) {
            throw new AccessDeniedException("Not authorized to access this account");
        }

        return account;
    }
}
```

### Your Project Status:
```java
// Current implementation - needs improvement
http.authorizeHttpRequests((requests) -> requests
    .requestMatchers("/account/**", "/balance/**", "/loan/**", "/card/**").authenticated()
    // ✅ Good: Requires authentication
    // ⚠️ Missing: Role-based checks, ownership validation
);
```

### Recommendations:
1. ✅ Use `@PreAuthorize` and `@Secured` for method-level security
2. ✅ Implement ownership checks in service layer
3. ✅ Use Spring Security's `@MethodSecurity` for fine-grained control
4. ✅ Validate user permissions before data access
5. ✅ Use UUIDs instead of sequential IDs to prevent enumeration

---

## 🥈 2. Security Misconfiguration (A02:2025)

### What It Is:
Security misconfiguration is the most common issue. This can happen at any level of an application stack, including the platform, web server, application server, database, framework, and custom code.

### Common Examples:
- **Default credentials** still enabled
- **Unnecessary features** enabled (debug mode, default accounts)
- **Missing security headers** (HSTS, X-Frame-Options, CSP)
- **Error messages** revealing stack traces
- **Unpatched frameworks** and dependencies
- **Open cloud storage** buckets

### Java/Spring Impact: 🔴 **CRITICAL**

### Vulnerable Code Examples:

```java
// ❌ BAD - Debug mode enabled in production
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        // Missing: spring.profiles.active=prod
    }
}

// ❌ BAD - Exposing stack traces
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        return ResponseEntity.status(500)
            .body(e.getMessage() + "\n" + e.getStackTrace()); // ❌ Exposes internals
    }
}

// ❌ BAD - Default security configuration
@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // ❌ Everything open!
        return http.build();
    }
}
```

### ✅ Secure Implementation:

```java
// ✅ GOOD - Profile-based configuration
@Configuration
@Profile("production")
public class ProductionSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated())
            .headers(headers -> headers
                .frameOptions().deny()
                .contentSecurityPolicy("default-src 'self'")
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)))
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}

// ✅ GOOD - Safe error handling
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        logger.error("Error occurred", e);

        // Don't expose stack trace in production
        String message = isProduction()
            ? "An error occurred. Please contact support."
            : e.getMessage();

        return ResponseEntity.status(500)
            .body(new ErrorResponse("INTERNAL_ERROR", message));
    }
}

// ✅ GOOD - Security headers configuration
@Configuration
public class SecurityHeadersConfig {
    @Bean
    public FilterRegistrationBean<HeaderFilter> securityHeadersFilter() {
        FilterRegistrationBean<HeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new HeaderFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
```

### Your Project Status:
```java
// Current issues to address:
// ⚠️ Missing security headers (HSTS, X-Frame-Options, CSP)
// ⚠️ No profile-based configuration
// ⚠️ Error handling might expose stack traces
```

### Recommendations:
1. ✅ Remove default accounts and change default passwords
2. ✅ Disable debug mode in production
3. ✅ Configure security headers (HSTS, CSP, X-Frame-Options)
4. ✅ Use environment-specific configurations
5. ✅ Keep dependencies updated
6. ✅ Disable unnecessary features

---

## 🥉 3. Software Supply Chain Failures (A03:2025) - **NEW**

### What It Is:
This new category addresses vulnerabilities arising from compromised software components, dependencies, and build processes. It highlights the importance of securing the entire software supply chain.

### Common Examples:
- **Vulnerable dependencies** with known CVEs
- **Compromised build tools** (Maven, Gradle)
- **Untrusted third-party libraries**
- **Malicious packages** in public repositories
- **Insecure CI/CD pipelines**

### Java/Spring Impact: 🔴 **CRITICAL**

### Vulnerable Code Examples:

```java
// ❌ BAD - Using vulnerable dependency versions
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-collections4</artifactId>
    <version>4.0</version> <!-- ❌ Known vulnerabilities -->
</dependency>

// ❌ BAD - No dependency scanning
// Missing: OWASP Dependency Check, Snyk, etc.
```

### ✅ Secure Implementation:

```java
// ✅ GOOD - Use dependency management tools
// pom.xml with dependency-check plugin
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>8.4.0</version>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>

// ✅ GOOD - Use Spring Boot BOM for managed dependencies
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.0</version> <!-- ✅ Latest secure version -->
</parent>

// ✅ GOOD - Verify dependency integrity
// Use checksums, signed artifacts
```

### Your Project Status:
```xml
<!-- Current pom.xml - needs dependency scanning -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.0</version> <!-- ✅ Good version -->
</parent>
<!-- ⚠️ Missing: OWASP Dependency Check plugin -->
```

### Recommendations:
1. ✅ Use OWASP Dependency-Check Maven plugin
2. ✅ Enable Snyk or Dependabot for automated scanning
3. ✅ Keep all dependencies updated
4. ✅ Use only trusted repositories (Maven Central)
5. ✅ Verify dependency checksums
6. ✅ Implement Software Bill of Materials (SBOM)
7. ✅ Scan CI/CD pipelines for vulnerabilities

---

## 4. Cryptographic Failures (A04:2025)

### What It Is:
Previously "Sensitive Data Exposure". Focuses on failures related to cryptography, such as weak encryption algorithms, improper key management, and data exposure.

### Common Examples:
- **Weak encryption algorithms** (MD5, SHA-1, DES)
- **Improper key management** (hardcoded keys, weak keys)
- **Sensitive data in logs** (passwords, credit cards, SSN)
- **Data transmitted over HTTP** instead of HTTPS
- **Insufficient encryption** for sensitive data at rest

### Java/Spring Impact: 🔴 **CRITICAL**

### Vulnerable Code Examples:

```java
// ❌ BAD - Weak password hashing
@Service
public class UserService {
    public void createUser(String username, String password) {
        // ❌ MD5 is broken and insecure
        String hash = DigestUtils.md5Hex(password);
        userRepository.save(new User(username, hash));
    }
}

// ❌ BAD - Hardcoded encryption key
@Service
public class EncryptionService {
    private static final String SECRET_KEY = "MySecretKey123"; // ❌ Hardcoded!

    public String encrypt(String data) {
        // Using hardcoded key
    }
}

// ❌ BAD - Logging sensitive data
@Service
public class PaymentService {
    public void processPayment(CreditCard card) {
        logger.info("Processing payment for card: {}", card.getNumber()); // ❌ Logs card number!
    }
}
```

### ✅ Secure Implementation:

```java
// ✅ GOOD - Strong password encoding
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // ✅ BCrypt with strength 12 (recommended)
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        // Uses: bcrypt, argon2, pbkdf2, scrypt
    }
}

// ✅ GOOD - Environment-based key management
@Service
public class EncryptionService {
    @Value("${encryption.secret-key}") // ✅ From environment
    private String secretKey;

    public String encrypt(String data) {
        // Use Java's strong encryption (AES-256-GCM)
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        // ...
    }
}

// ✅ GOOD - Safe logging
@Service
public class PaymentService {
    public void processPayment(CreditCard card) {
        // ✅ Mask sensitive data
        String maskedCard = maskCardNumber(card.getNumber());
        logger.info("Processing payment for card: {}", maskedCard);
        // Logs: "Processing payment for card: ****-****-****-1234"
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }
}
```

### Your Project Status:
```java
// ✅ GOOD - Using BCrypt password encoder
@Bean
public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}

// ✅ GOOD - Compromised password checker
@Bean
public CompromisedPasswordChecker compromisedPasswordChecker() {
    return new HaveIBeenPwnedRestApiPasswordChecker();
}
```

### Recommendations:
1. ✅ Use strong password hashing (BCrypt, Argon2, PBKDF2)
2. ✅ Store encryption keys in secure vaults (HashiCorp Vault, AWS KMS)
3. ✅ Use HTTPS/TLS 1.3 for all communications
4. ✅ Mask sensitive data in logs
5. ✅ Encrypt sensitive data at rest
6. ✅ Use Java's `javax.crypto` with strong algorithms (AES-256-GCM)
7. ✅ Rotate encryption keys regularly

---

## 5. Injection (A05:2025)

### What It Is:
Injection flaws occur when untrusted data is sent to an interpreter as part of a command or query. The attacker's hostile data can trick the interpreter into executing unintended commands or accessing unauthorized data.

### Common Examples:
- **SQL Injection** (SQLi)
- **NoSQL Injection**
- **Command Injection**
- **LDAP Injection**
- **XPath Injection**

### Java/Spring Impact: 🔴 **CRITICAL**

### Vulnerable Code Examples:

```java
// ❌ BAD - SQL Injection
@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User findByUsername(String username) {
        // ❌ SQL Injection vulnerability!
        String sql = "SELECT * FROM users WHERE username = '" + username + "'";
        return jdbcTemplate.queryForObject(sql, User.class);
        // Attacker can input: admin' OR '1'='1
    }
}

// ❌ BAD - Command Injection
@Service
public class SystemService {
    public String executeCommand(String command) {
        // ❌ Command injection!
        Process process = Runtime.getRuntime().exec("ping " + command);
        // Attacker can input: 8.8.8.8; rm -rf /
    }
}

// ❌ BAD - NoSQL Injection
@Repository
public class UserRepository {
    public User findByEmail(String email) {
        // ❌ NoSQL Injection!
        Query query = new Query();
        query.addCriteria(Criteria.where("email").is(email));
        // Attacker can pass: {"$ne": null} to bypass authentication
    }
}
```

### ✅ Secure Implementation:

```java
// ✅ GOOD - Parameterized queries (JPA)
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // ✅ Spring Data JPA uses parameterized queries automatically
    User findByUsername(String username);

    // ✅ Custom query with parameters
    @Query("SELECT u FROM User u WHERE u.username = :username")
    User findByUsernameParam(@Param("username") String username);
}

// ✅ GOOD - JdbcTemplate with parameters
@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User findByUsername(String username) {
        // ✅ Parameterized query - safe from SQL injection
        String sql = "SELECT * FROM users WHERE username = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{username}, User.class);
    }
}

// ✅ GOOD - Input validation
@RestController
public class UserController {
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable @Min(1) @Max(Long.MAX_VALUE) Long id) {
        // ✅ Validated input
        return userService.findById(id);
    }

    @PostMapping("/users")
    public User createUser(@Valid @RequestBody UserRequest request) {
        // ✅ @Valid ensures all constraints are checked
        return userService.create(request);
    }
}

// ✅ GOOD - Safe command execution (avoid if possible)
@Service
public class SystemService {
    private static final List<String> ALLOWED_COMMANDS = List.of("ping", "traceroute");

    public String executeCommand(String command) {
        // ✅ Whitelist approach
        if (!ALLOWED_COMMANDS.contains(command)) {
            throw new IllegalArgumentException("Command not allowed");
        }

        // ✅ Use ProcessBuilder with arguments array
        ProcessBuilder pb = new ProcessBuilder("ping", "-c", "4", command);
        Process process = pb.start();
        // ...
    }
}
```

### Your Project Status:
```java
// ✅ GOOD - Using Spring Data JPA (parameterized queries)
// ✅ GOOD - Input validation with @Valid, @Pattern
// ⚠️ Review: Ensure all user inputs are validated
```

### Recommendations:
1. ✅ Use Spring Data JPA (automatic parameterization)
2. ✅ Always use parameterized queries/prepared statements
3. ✅ Validate and sanitize all user inputs
4. ✅ Use whitelist approach for command execution
5. ✅ Implement input validation with Bean Validation
6. ✅ Use ORM frameworks (Hibernate) instead of raw SQL
7. ✅ Escape special characters in user inputs

---

## 6. Insecure Design (A06:2025)

### What It Is:
Insecure design is a different category from insecure implementation. It represents missing or ineffective control design. This category focuses on risks related to design and architectural flaws.

### Common Examples:
- **Missing threat modeling**
- **Insecure authentication flows**
- **Weak password policies**
- **No rate limiting**
- **Missing security controls** in design phase

### Java/Spring Impact: 🟡 **HIGH**

### Vulnerable Design Examples:

```java
// ❌ BAD - No rate limiting on login
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    // ❌ Vulnerable to brute force attacks
    return authService.authenticate(request);
}

// ❌ BAD - Weak password policy
public class User {
    @Size(min = 4) // ❌ Too weak!
    private String password;
}

// ❌ BAD - No account lockout
@Service
public class AuthService {
    public boolean authenticate(String username, String password) {
        // ❌ No lockout after failed attempts
        return userRepository.findByUsername(username)
            .map(user -> passwordEncoder.matches(password, user.getPassword()))
            .orElse(false);
    }
}
```

### ✅ Secure Design:

```java
// ✅ GOOD - Rate limiting with Spring Security
@Configuration
public class SecurityConfig {
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter());
        registration.addUrlPatterns("/login", "/register");
        return registration;
    }
}

// ✅ GOOD - Strong password policy
public class UserRegistrationRequest {
    @NotBlank
    @Size(min = 8, max = 20)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
             message = "Password must contain uppercase, lowercase, digit, and special character")
    private String password;
}

// ✅ GOOD - Account lockout mechanism
@Service
public class AuthService {
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION = 15 * 60 * 1000; // 15 minutes

    public boolean authenticate(String username, String password) {
        // Check if account is locked
        if (isAccountLocked(username)) {
            throw new AccountLockedException("Account is locked. Try again later.");
        }

        boolean authenticated = userRepository.findByUsername(username)
            .map(user -> passwordEncoder.matches(password, user.getPassword()))
            .orElse(false);

        if (!authenticated) {
            recordFailedAttempt(username);
            throw new BadCredentialsException("Invalid credentials");
        }

        // Reset on successful login
        failedAttempts.remove(username);
        return true;
    }

    private boolean isAccountLocked(String username) {
        Integer attempts = failedAttempts.get(username);
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }
}
```

### Recommendations:
1. ✅ Perform threat modeling during design phase
2. ✅ Implement strong password policies
3. ✅ Add rate limiting to authentication endpoints
4. ✅ Implement account lockout mechanisms
5. ✅ Design with security in mind (defense in depth)
6. ✅ Use secure design patterns
7. ✅ Regular security architecture reviews

---

## 7. Authentication Failures (A07:2025)

### What It Is:
Previously "Identification and Authentication Failures". Focuses on weaknesses in authentication mechanisms that can lead to unauthorized access.

### Common Examples:
- **Weak passwords** and no password policy
- **Missing multi-factor authentication (MFA)**
- **Session fixation** vulnerabilities
- **Weak session management**
- **Credential stuffing** attacks
- **Password reset flaws**

### Java/Spring Impact: 🔴 **CRITICAL**

### Vulnerable Code Examples:

```java
// ❌ BAD - Weak session management
@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            // ❌ No timeout, no concurrent session control
        );
        return http.build();
    }
}

// ❌ BAD - No password complexity
public class User {
    @Size(min = 3) // ❌ Too weak!
    private String password;
}

// ❌ BAD - Predictable session IDs
// Using default session ID generation
```

### ✅ Secure Implementation:

```java
// ✅ GOOD - Strong session management
@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1) // ✅ Prevent concurrent sessions
                .maxSessionsPreventsLogin(true)
                .sessionRegistry(sessionRegistry())
                .and()
                .sessionFixation().migrateSession() // ✅ Prevent session fixation
                .invalidSessionUrl("/login?expired")
                .sessionTimeout(Duration.ofMinutes(30)))
            .rememberMe(rememberMe -> rememberMe
                .tokenValiditySeconds(86400) // 24 hours
                .key("uniqueAndSecretKey"));
        return http.build();
    }
}

// ✅ GOOD - Strong password policy (already in your project)
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$")
private String password;

// ✅ GOOD - MFA support
@Service
public class MfaService {
    public void enableMfa(String username) {
        String secret = totpService.generateSecret();
        userRepository.updateMfaSecret(username, secret);
        // Generate QR code for user to scan
    }

    public boolean verifyMfaCode(String username, String code) {
        String secret = userRepository.getMfaSecret(username);
        return totpService.verifyCode(secret, code);
    }
}
```

### Your Project Status:
```java
// ✅ GOOD - Strong password encoder
@Bean
public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}

// ✅ GOOD - Compromised password checker
@Bean
public CompromisedPasswordChecker compromisedPasswordChecker() {
    return new HaveIBeenPwnedRestApiPasswordChecker();
}

// ⚠️ Missing: Session timeout, concurrent session control, MFA
```

### Recommendations:
1. ✅ Implement strong password policies
2. ✅ Use secure password hashing (BCrypt, Argon2)
3. ✅ Implement session timeout and concurrent session control
4. ✅ Add multi-factor authentication (MFA)
5. ✅ Prevent session fixation attacks
6. ✅ Implement account lockout after failed attempts
7. ✅ Use secure session ID generation
8. ✅ Check passwords against breached password databases

---

## 8. Software and Data Integrity Failures (A08:2025)

### What It Is:
Previously "Insecure Deserialization". Focuses on failures related to software updates, critical data, and CI/CD pipelines not being protected against integrity violations.

### Common Examples:
- **Insecure deserialization**
- **Unsigned software updates**
- **Compromised CI/CD pipelines**
- **Untrusted data sources**
- **Missing integrity checks**

### Java/Spring Impact: 🟡 **HIGH**

### Vulnerable Code Examples:

```java
// ❌ BAD - Insecure deserialization
@Service
public class DataService {
    public Object deserialize(byte[] data) {
        // ❌ Dangerous - can execute arbitrary code
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        return ois.readObject();
    }
}

// ❌ BAD - No integrity verification
@Service
public class UpdateService {
    public void updateSoftware(byte[] updateFile) {
        // ❌ No signature verification
        // Install update directly
    }
}
```

### ✅ Secure Implementation:

```java
// ✅ GOOD - Safe deserialization
@Service
public class DataService {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public <T> T deserialize(byte[] data, Class<T> clazz) {
        // ✅ Use JSON instead of Java serialization
        try {
            return objectMapper.readValue(data, clazz);
        } catch (Exception e) {
            throw new DeserializationException("Failed to deserialize", e);
        }
    }
}

// ✅ GOOD - Integrity verification
@Service
public class UpdateService {
    public void updateSoftware(byte[] updateFile, String signature) {
        // ✅ Verify digital signature
        if (!verifySignature(updateFile, signature)) {
            throw new SecurityException("Update signature verification failed");
        }
        // Install update
    }

    private boolean verifySignature(byte[] data, String signature) {
        // Use Java's Signature API
        // Verify against public key
        return true;
    }
}
```

### Recommendations:
1. ✅ Avoid Java serialization for untrusted data
2. ✅ Use JSON/XML for data exchange
3. ✅ Verify digital signatures for software updates
4. ✅ Secure CI/CD pipelines
5. ✅ Implement integrity checks (checksums, hashes)
6. ✅ Use trusted data sources only
7. ✅ Implement code signing

---

## 9. Logging and Alerting Failures (A09:2025) - **NEW**

### What It Is:
Previously part of "Insufficient Logging & Monitoring". Focuses on the lack of proper logging and monitoring, which can delay detection of security incidents.

### Common Examples:
- **Missing security event logging**
- **Insufficient log retention**
- **No alerting on suspicious activities**
- **Logs not monitored**
- **Sensitive data in logs**

### Java/Spring Impact: 🟡 **HIGH**

### Vulnerable Code Examples:

```java
// ❌ BAD - No security event logging
@Service
public class AuthService {
    public boolean login(String username, String password) {
        boolean success = authenticate(username, password);
        // ❌ No logging of login attempts
        return success;
    }
}

// ❌ BAD - Logging sensitive data
@Service
public class PaymentService {
    public void processPayment(CreditCard card) {
        logger.info("Processing payment: {}", card); // ❌ Logs full card object
    }
}
```

### ✅ Secure Implementation:

```java
// ✅ GOOD - Comprehensive security logging
@Service
public class AuthService {
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    public boolean login(String username, String password) {
        boolean success = authenticate(username, password);

        if (success) {
            securityLogger.info("Successful login: username={}, ip={}, timestamp={}",
                username, getClientIp(), Instant.now());
        } else {
            securityLogger.warn("Failed login attempt: username={}, ip={}, timestamp={}",
                username, getClientIp(), Instant.now());
            // ✅ Alert on multiple failed attempts
            checkAndAlertFailedAttempts(username);
        }

        return success;
    }
}

// ✅ GOOD - Safe logging (already in your project)
@Service
public class AccountService {
    public Map<String, String> getAllAccounts() {
        logger.info("Retrieved {} accounts successfully", accounts.size());
        // ✅ Logs count, not sensitive data
    }
}

// ✅ GOOD - Structured logging for monitoring
@Aspect
@Component
public class SecurityEventAspect {
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    @AfterReturning("@annotation(PreAuthorize)")
    public void logAuthorization(JoinPoint joinPoint) {
        securityLogger.info("Authorization check: method={}, user={}, result=ALLOWED",
            joinPoint.getSignature().getName(), getCurrentUser());
    }

    @AfterThrowing(pointcut = "@annotation(PreAuthorize)", throwing = "ex")
    public void logAuthorizationFailure(JoinPoint joinPoint, Exception ex) {
        securityLogger.warn("Authorization denied: method={}, user={}, reason={}",
            joinPoint.getSignature().getName(), getCurrentUser(), ex.getMessage());
        // ✅ Alert security team
        alertSecurityTeam(joinPoint, ex);
    }
}
```

### Your Project Status:
```java
// ✅ GOOD - Comprehensive logging implemented
// ✅ GOOD - Structured logging with SLF4J
// ✅ GOOD - Log levels configured properly
// ⚠️ Missing: Security event logging, alerting mechanism
```

### Recommendations:
1. ✅ Log all security events (login, authorization, data access)
2. ✅ Implement log aggregation (ELK, Splunk)
3. ✅ Set up alerts for suspicious activities
4. ✅ Never log sensitive data (passwords, tokens, PII)
5. ✅ Use structured logging (JSON format)
6. ✅ Implement log retention policies
7. ✅ Monitor logs in real-time
8. ✅ Set up SIEM (Security Information and Event Management)

---

## 10. Mishandling of Exceptional Conditions (A10:2025) - **NEW**

### What It Is:
A new category focusing on improper error handling and logic errors that can be exploited by attackers.

### Common Examples:
- **Information disclosure** in error messages
- **Logic errors** leading to security bypass
- **Improper exception handling**
- **Stack trace exposure**
- **Error messages revealing system internals**

### Java/Spring Impact: 🟡 **HIGH**

### Vulnerable Code Examples:

```java
// ❌ BAD - Exposing stack traces
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        // ❌ Exposes full stack trace
        return ResponseEntity.status(500)
            .body(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
    }
}

// ❌ BAD - Information disclosure
@Service
public class UserService {
    public User findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            // ❌ Reveals that username doesn't exist
            throw new UserNotFoundException("User " + username + " not found");
        }
        return user;
    }
}

// ❌ BAD - Logic error
@Service
public class PaymentService {
    public void processPayment(PaymentRequest request) {
        if (request.getAmount() < 0) {
            // ❌ Logic error - should reject negative amounts
            request.setAmount(Math.abs(request.getAmount()));
        }
        // Process payment
    }
}
```

### ✅ Secure Implementation:

```java
// ✅ GOOD - Safe error handling
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        // ✅ Log full details server-side
        logger.error("Error processing request: {}", request.getRequestURI(), e);

        // ✅ Return generic message to client
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            isProduction() ? "An error occurred. Please contact support." : e.getMessage(),
            UUID.randomUUID().toString() // Error ID for support
        );

        return ResponseEntity.status(500).body(error);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        // ✅ Generic message - doesn't reveal if user exists
        return ResponseEntity.status(401)
            .body(new ErrorResponse("AUTH_ERROR", "Invalid credentials"));
    }
}

// ✅ GOOD - Proper validation
@Service
public class PaymentService {
    public void processPayment(@Valid PaymentRequest request) {
        // ✅ Validation ensures amount > 0
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Payment amount must be positive");
        }
        // Process payment
    }
}

// ✅ GOOD - Error response DTO
public class ErrorResponse {
    private String error;
    private String message;
    private String errorId; // For support tracking
    private Instant timestamp;

    // Constructors, getters, setters
}
```

### Your Project Status:
```java
// ⚠️ Review: Ensure error handling doesn't expose sensitive information
// ⚠️ Review: Check for logic errors in business logic
```

### Recommendations:
1. ✅ Never expose stack traces to clients
2. ✅ Use generic error messages in production
3. ✅ Log detailed errors server-side only
4. ✅ Implement proper exception hierarchy
5. ✅ Validate all inputs to prevent logic errors
6. ✅ Use error IDs for support tracking
7. ✅ Don't reveal system internals in error messages
8. ✅ Handle exceptions at appropriate levels

---

## Summary: OWASP Top 10 2025 for Java/Spring

### Priority Matrix

| Rank | Category | Java/Spring Impact | Your Project Status |
|------|----------|-------------------|---------------------|
| 1 | Broken Access Control | 🔴 Critical | ⚠️ Needs improvement |
| 2 | Security Misconfiguration | 🔴 Critical | ⚠️ Needs improvement |
| 3 | Software Supply Chain | 🔴 Critical | ⚠️ Add dependency scanning |
| 4 | Cryptographic Failures | 🔴 Critical | ✅ Good (BCrypt, password checker) |
| 5 | Injection | 🔴 Critical | ✅ Good (JPA, validation) |
| 6 | Insecure Design | 🟡 High | ⚠️ Add rate limiting, lockout |
| 7 | Authentication Failures | 🔴 Critical | ✅ Good (password policy) ⚠️ Add MFA |
| 8 | Software/Data Integrity | 🟡 High | ✅ Good (avoid serialization) |
| 9 | Logging & Alerting | 🟡 High | ✅ Good logging ⚠️ Add security events |
| 10 | Exceptional Conditions | 🟡 High | ⚠️ Review error handling |

### Quick Action Items for Your Project

1. ✅ **Add method-level security** (`@PreAuthorize`)
2. ✅ **Configure security headers** (HSTS, CSP, X-Frame-Options)
3. ✅ **Add OWASP Dependency Check** plugin
4. ✅ **Implement rate limiting** on auth endpoints
5. ✅ **Add account lockout** mechanism
6. ✅ **Enhance error handling** (no stack traces)
7. ✅ **Add security event logging**
8. ✅ **Implement session management** (timeout, concurrent sessions)

---

## Tools for Java/Spring Security

### Dependency Scanning
- **OWASP Dependency-Check** - Maven plugin
- **Snyk** - Automated vulnerability scanning
- **Dependabot** - GitHub integration

### Security Testing
- **OWASP ZAP** - Dynamic application security testing
- **Burp Suite** - Web vulnerability scanner
- **SonarQube** - Code quality and security

### Static Analysis
- **SpotBugs** - Find bugs in Java code
- **PMD** - Source code analyzer
- **Checkmarx** - SAST tool

---

## Resources

- [OWASP Top 10 2025](https://owasp.org/Top10/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [OWASP Java Project](https://owasp.org/www-project-java/)
- [CWE Top 25](https://cwe.mitre.org/top25/)

---

**Remember**: Security is not a one-time task but an ongoing process. Regular security audits, dependency updates, and security training are essential! 🔒

