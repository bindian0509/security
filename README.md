# Spring Security Learning Project

A Spring Boot application demonstrating Spring Security fundamentals and authentication mechanisms.

## 📋 Table of Contents

- [Overview](#overview)
- [Technologies](#technologies)
- [Spring Security Modules](#spring-security-modules)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
- [Authentication](#authentication)

## 🎯 Overview

This project is designed to learn and experiment with Spring Security features. It demonstrates basic authentication, security configuration, and protected REST endpoints.

## 🛠 Technologies

- **Java 21**
- **Spring Boot 4.0.0**
- **Spring Security** - Authentication and authorization framework
- **Spring Web MVC** - RESTful web services
- **Maven** - Dependency management

## 🔐 Spring Security Modules

This project utilizes the following Spring Security components:

### 1. **Spring Security Auto-Configuration**

- Automatic security configuration enabled by `spring-boot-starter-security`
- Default security filter chain
- Automatic CSRF protection
- Session management

### 2. **Authentication**

- **In-Memory Authentication**: Configured via `application.properties`
- **Default User**: Auto-generated user with random password (when no custom user is configured)
- **Custom User**: Configurable username and password through properties

### 3. **Security Filter Chain**

- All endpoints are secured by default
- HTTP Basic Authentication enabled
- Automatic login page generation (if not using REST)

### 4. **Logging & Debugging**

- Security-related logging configured at INFO level
- Console logging with colored output for better visibility

## 📁 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/bharat/security/
│   │       ├── SecurityApplication.java          # Main application class
│   │       ├── controller/
│   │       │   ├── AccountController.java        # Account management endpoints
│   │       │   └── SecurityController.java       # Welcome endpoint
│   │       └── service/
│   │           └── AccountService.java           # Business logic for accounts
│   └── resources/
│       └── application.properties                 # Application configuration
└── test/
    └── java/
        └── com/bharat/security/
            └── SecurityApplicationTests.java      # Unit tests
```

## ⚙️ Configuration

### Application Properties

The security configuration is defined in `src/main/resources/application.properties`:

```properties
# Application name
spring.application.name=${SPRING_APP_NAME:security}

# Security Configuration
spring.security.user.name=${SECURITY_USERNAME:bharat}
spring.security.user.password=${SECURITY_PASSWORD:123456}

# Logging Configuration
logging.level.org.springframework.boot.autoconfigure.security=INFO
logging.level.org.springframework.security=INFO
logging.pattern.console=%green(%d{HH:mm:ss.SSS}) %blue(%-5level) %red([%thread]) %yellow(%logger{15}) - %msg%n
```

### Environment Variables

You can override security credentials using environment variables:

- `SECURITY_USERNAME` - Override default username
- `SECURITY_PASSWORD` - Override default password
- `SPRING_APP_NAME` - Override application name

## 🌐 API Endpoints

### Public Endpoints

All endpoints require HTTP Basic Authentication.

#### 1. Welcome Endpoint

- **URL**: `GET /welcome`
- **Description**: Returns a welcome message
- **Response**:
  ```json
  "Welcome to Spring Boot Application"
  ```

#### 2. Get Account Details

- **URL**: `GET /account/{accountId}`
- **Description**: Retrieves account details by ID
- **Path Parameter**: `accountId` (String)
- **Example**: `GET /account/12345`
- **Response**:
  ```
  "The Account Id 12345 has been closed due to fraud charges!"
  ```

#### 3. Get All Accounts

- **URL**: `GET /account`
- **Description**: Retrieves all accounts
- **Response**:
  ```json
  {
    "Sita Ram Deewan Chand": "00420",
    "Choley Bhature wala": "00421",
    "Choley Kulche Wala": "00422"
  }
  ```

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+ (or use the included Maven wrapper)

### Running the Application

1. **Clone the repository** (if applicable)

2. **Build the project**:

   ```bash
   ./mvnw clean install
   ```

3. **Run the application**:

   ```bash
   ./mvnw spring-boot:run
   ```

   Or using Java directly:

   ```bash
   java -jar target/security-0.0.1-SNAPSHOT.jar
   ```

4. **Access the application**:
   - The application will start on `http://localhost:8080` (default port)

### Using Maven Wrapper

- **Linux/Mac**: `./mvnw`
- **Windows**: `mvnw.cmd`

## 🔑 Authentication

### Default Authentication

By default, Spring Security provides:

- **Username**: `user`
- **Password**: Randomly generated (printed in console logs)

To see the auto-generated password, comment out the custom user configuration in `application.properties`:

```properties
# spring.security.user.name=${SECURITY_USERNAME:bharat}
# spring.security.user.password=${SECURITY_PASSWORD:123456}
```

### Custom Authentication

The application is configured with:

- **Username**: `bharat` (or value from `SECURITY_USERNAME` env variable)
- **Password**: `123456` (or value from `SECURITY_PASSWORD` env variable)

### Making API Requests

#### Using cURL

```bash
# Get welcome message
curl -u bharat:123456 http://localhost:8080/welcome

# Get account details
curl -u bharat:123456 http://localhost:8080/account/12345

# Get all accounts
curl -u bharat:123456 http://localhost:8080/account
```

#### Using Browser

When accessing endpoints through a browser, you'll be prompted for credentials:

- Username: `bharat`
- Password: `123456`

#### Using Postman/Insomnia

1. Select **Basic Auth** as the authentication type
2. Enter username: `bharat`
3. Enter password: `123456`
4. Make your request

## 📚 Spring Security Features Demonstrated

1. **Automatic Security Configuration**: Spring Boot auto-configures security
2. **HTTP Basic Authentication**: All endpoints require authentication
3. **In-Memory User Management**: Simple user store via properties
4. **Security Filter Chain**: Automatic request filtering
5. **CSRF Protection**: Enabled by default
6. **Session Management**: Automatic session handling

## 🔍 Logging

Security-related logs are configured to show:

- Security filter chain initialization
- Authentication attempts
- Authorization decisions
- Auto-configuration details

Look for log messages like:

```
Using generated security password: <random-password>
```

## 🧪 Testing

Run tests using:

```bash
./mvnw test
```

## 📝 Notes

- All endpoints are secured by default
- To disable security for specific endpoints, you'll need to create a custom `SecurityFilterChain` configuration
- The default security configuration provides a good starting point for learning Spring Security

## 🔄 Future Enhancements

Potential areas for expansion:

- Custom `SecurityFilterChain` configuration
- Role-based access control (RBAC)
- JWT token authentication
- OAuth2 integration
- Method-level security
- Custom authentication providers
- Password encoding strategies

## 📄 License

This is a learning project.

## 👤 Author

Bharat Verma

---

**Happy Learning! 🎓**
