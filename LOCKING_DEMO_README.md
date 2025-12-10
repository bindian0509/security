# Optimistic and Pessimistic Locking Demo

## Overview

This project demonstrates **Optimistic Locking** and **Pessimistic Locking** implementations using both **MySQL (JPA)** and **MongoDB**.

### What is Locking?

**Locking** prevents concurrent modifications from causing data inconsistencies (lost updates, dirty reads).

#### Optimistic Locking
- ✅ **Assumes conflicts are rare**
- ✅ **Uses version field** (`@Version` annotation)
- ✅ **Fast** - no database locks
- ✅ **Fails with exception** if version mismatch
- ✅ **Best for**: High read/low write scenarios

#### Pessimistic Locking
- ✅ **Assumes conflicts are common**
- ✅ **Locks database row/document** (SELECT FOR UPDATE)
- ✅ **Slower** - other transactions wait
- ✅ **Guarantees** no conflicts
- ✅ **Best for**: High write scenarios, critical data

---

## Architecture

```
Application
    ├── MySQL (JPA/Hibernate)
    │   ├── Product Entity (@Version for optimistic)
    │   ├── @Lock(LockModeType.PESSIMISTIC_WRITE) for pessimistic
    │   └── ProductService (demonstrates both)
    │
    └── MongoDB
        ├── Inventory Document (@Version for optimistic)
        ├── Transactions for pessimistic
        └── InventoryService (demonstrates both)
```

---

## Prerequisites

- Docker and Docker Compose
- Java 21
- Maven 3.8+

---

## Quick Start

### 1. Start Databases with Docker

```bash
# Start MySQL and MongoDB
docker-compose up -d

# Verify containers are running
docker-compose ps

# View logs
docker-compose logs -f
```

### 2. Configure Application

The application is already configured to connect to:
- **MySQL**: `localhost:3306`
- **MongoDB**: `localhost:27017`

Environment variables (optional):
```bash
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=security_db
export MYSQL_USER=root
export MYSQL_PASSWORD=rootpassword

export MONGODB_HOST=localhost
export MONGODB_PORT=27017
export MONGODB_DATABASE=security_db
export MONGODB_USER=admin
export MONGODB_PASSWORD=adminpassword
```

### 3. Start Application

```bash
mvn spring-boot:run
```

### 4. Test Locking Mechanisms

See [Testing Scenarios](#testing-scenarios) below.

---

## API Endpoints

### MySQL Product Endpoints

#### Create Product
```http
POST /api/locking/mysql/products
Content-Type: application/json

{
  "name": "Laptop",
  "description": "Gaming Laptop",
  "price": 1299.99,
  "stock": 10
}
```

#### Get Product
```http
GET /api/locking/mysql/products/{id}
```

#### Update with Optimistic Locking
```http
PUT /api/locking/mysql/products/{id}/optimistic
Content-Type: application/json

{
  "name": "Updated Laptop",
  "price": 1199.99,
  "stock": 8
}
```

#### Update with Pessimistic Locking
```http
PUT /api/locking/mysql/products/{id}/pessimistic
Content-Type: application/json

{
  "name": "Updated Laptop",
  "price": 1199.99,
  "stock": 8
}
```

#### Decrease Stock (Optimistic)
```http
POST /api/locking/mysql/products/{id}/decrease-stock/optimistic
Content-Type: application/json

{
  "quantity": 2
}
```

#### Decrease Stock (Pessimistic)
```http
POST /api/locking/mysql/products/{id}/decrease-stock/pessimistic
Content-Type: application/json

{
  "quantity": 2
}
```

### MongoDB Inventory Endpoints

#### Create Inventory Item
```http
POST /api/locking/mongodb/inventory
Content-Type: application/json

{
  "itemName": "Smartphone",
  "category": "Electronics",
  "price": 699.99,
  "quantity": 20
}
```

#### Get Inventory
```http
GET /api/locking/mongodb/inventory/{id}
```

#### Update with Optimistic Locking
```http
PUT /api/locking/mongodb/inventory/{id}/optimistic
Content-Type: application/json

{
  "itemName": "Updated Smartphone",
  "price": 649.99,
  "quantity": 18
}
```

#### Update with Pessimistic Locking
```http
PUT /api/locking/mongodb/inventory/{id}/pessimistic
Content-Type: application/json

{
  "itemName": "Updated Smartphone",
  "price": 649.99,
  "quantity": 18
}
```

#### Decrease Quantity (Optimistic)
```http
POST /api/locking/mongodb/inventory/{id}/decrease-quantity/optimistic
Content-Type: application/json

{
  "quantity": 3
}
```

#### Decrease Quantity (Pessimistic)
```http
POST /api/locking/mongodb/inventory/{id}/decrease-quantity/pessimistic
Content-Type: application/json

{
  "quantity": 3
}
```

---

## Testing Scenarios

### Scenario 1: Optimistic Locking - Concurrent Updates

**Test**: Two users try to update the same product simultaneously.

```bash
# Terminal 1
curl -X PUT http://localhost:8080/api/locking/mysql/products/1/optimistic \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop V1","price":1200,"stock":9}'

# Terminal 2 (run immediately after Terminal 1)
curl -X PUT http://localhost:8080/api/locking/mysql/products/1/optimistic \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop V2","price":1100,"stock":8}'
```

**Expected Result:**
- ✅ First request succeeds
- ❌ Second request gets `409 Conflict` with `OptimisticLockException`
- Message: "Product was modified by another transaction. Please refresh and try again."

### Scenario 2: Pessimistic Locking - Concurrent Updates

**Test**: Two users try to update the same product simultaneously.

```bash
# Terminal 1
curl -X PUT http://localhost:8080/api/locking/mysql/products/1/pessimistic \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop V1","price":1200,"stock":9}'

# Terminal 2 (run immediately - will wait)
curl -X PUT http://localhost:8080/api/locking/mysql/products/1/pessimistic \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop V2","price":1100,"stock":8}'
```

**Expected Result:**
- ✅ First request succeeds (takes ~2 seconds)
- ✅ Second request waits, then succeeds
- ✅ No conflicts - both updates applied sequentially

### Scenario 3: E-commerce Stock Decrease (Optimistic)

**Test**: Multiple users trying to buy the last item.

```bash
# Create product with stock = 1
curl -X POST http://localhost:8080/api/locking/mysql/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Last Item","description":"Only 1 left","price":100,"stock":1}'

# Get the ID from response, then:
# Terminal 1
curl -X POST http://localhost:8080/api/locking/mysql/products/1/decrease-stock/optimistic \
  -H "Content-Type: application/json" \
  -d '{"quantity":1}'

# Terminal 2 (run immediately)
curl -X POST http://localhost:8080/api/locking/mysql/products/1/decrease-stock/optimistic \
  -H "Content-Type: application/json" \
  -d '{"quantity":1}'
```

**Expected Result:**
- ✅ First request succeeds (stock becomes 0)
- ❌ Second request gets `409 Conflict` (prevents overselling)

### Scenario 4: E-commerce Stock Decrease (Pessimistic)

Same test as Scenario 3, but with pessimistic locking:

```bash
# Terminal 1
curl -X POST http://localhost:8080/api/locking/mysql/products/1/decrease-stock/pessimistic \
  -H "Content-Type: application/json" \
  -d '{"quantity":1}'

# Terminal 2 (waits for Terminal 1 to complete)
curl -X POST http://localhost:8080/api/locking/mysql/products/1/decrease-stock/pessimistic \
  -H "Content-Type: application/json" \
  -d '{"quantity":1}'
```

**Expected Result:**
- ✅ First request succeeds
- ✅ Second request waits, then fails with "Insufficient stock" (stock already 0)

---

## Implementation Details

### MySQL (JPA) Optimistic Locking

```java
@Entity
public class Product {
    @Version
    private Long version;  // Automatically incremented on update
}

// Usage
Product product = repository.findById(id).get();
product.setName("New Name");
repository.save(product);  // Checks version - throws OptimisticLockException if changed
```

### MySQL (JPA) Pessimistic Locking

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Optional<Product> findByIdWithPessimisticLock(@Param("id") Long id);

// Usage
Product product = repository.findByIdWithPessimisticLock(id).get();
// Row is locked until transaction completes
product.setName("New Name");
repository.save(product);
```

### MongoDB Optimistic Locking

```java
@Document
public class Inventory {
    @Version
    private Long version;  // Automatically incremented on update
}

// Usage
Inventory inventory = repository.findById(id).get();
inventory.setQuantity(10);
repository.save(inventory);  // Checks version - throws OptimisticLockingFailureException if changed
```

### MongoDB Pessimistic Locking

```java
// Uses MongoDB transactions
@Transactional
public Inventory updateInventoryPessimistic(String id, ...) {
    // Transaction locks document
    Inventory inventory = mongoTemplate.findOne(query, Inventory.class);
    // Update within transaction
    mongoTemplate.updateFirst(query, update, Inventory.class);
}
```

---

## Docker Commands

### Start Databases
```bash
docker-compose up -d
```

### Stop Databases
```bash
docker-compose down
```

### Stop and Remove Volumes
```bash
docker-compose down -v
```

### View Logs
```bash
docker-compose logs -f mysql
docker-compose logs -f mongodb
```

### Access MySQL
```bash
docker exec -it security-mysql mysql -uroot -prootpassword security_db
```

### Access MongoDB
```bash
docker exec -it security-mongodb mongosh -u admin -p adminpassword --authenticationDatabase admin
```

### Start with Admin Tools (phpMyAdmin, Mongo Express)
```bash
docker-compose --profile tools up -d
```

Then access:
- **phpMyAdmin**: http://localhost:8082
- **Mongo Express**: http://localhost:8081

---

## Database Credentials

### MySQL
- **Host**: localhost
- **Port**: 3306
- **Database**: security_db
- **Root User**: root
- **Root Password**: rootpassword
- **App User**: appuser
- **App Password**: apppassword

### MongoDB
- **Host**: localhost
- **Port**: 27017
- **Database**: security_db
- **Admin User**: admin
- **Admin Password**: adminpassword

---

## Key Differences: Optimistic vs Pessimistic

| Aspect | Optimistic | Pessimistic |
|--------|-----------|-------------|
| **Lock Type** | Version field | Database row lock |
| **Performance** | Fast (no locks) | Slower (locks held) |
| **Conflict Detection** | At commit time | Prevents conflicts |
| **Failure Mode** | Exception on conflict | Waits/blocks |
| **Best For** | High read, low write | High write, critical data |
| **Retry Needed** | Yes (on exception) | No (waits automatically) |
| **Scalability** | Better | Lower (locks reduce concurrency) |

---

## Best Practices

### When to Use Optimistic Locking:
✅ High read-to-write ratio
✅ Conflicts are rare
✅ Performance is critical
✅ Can handle retries in application
✅ Example: Product catalog updates

### When to Use Pessimistic Locking:
✅ High write-to-read ratio
✅ Conflicts are common
✅ Data integrity is critical
✅ Cannot afford lost updates
✅ Example: Financial transactions, inventory management

---

## Troubleshooting

### MySQL Connection Error
```bash
# Check if MySQL is running
docker-compose ps mysql

# Check MySQL logs
docker-compose logs mysql

# Verify connection
mysql -h localhost -P 3306 -u root -prootpassword
```

### MongoDB Connection Error
```bash
# Check if MongoDB is running
docker-compose ps mongodb

# Check MongoDB logs
docker-compose logs mongodb

# Verify connection
mongosh "mongodb://admin:adminpassword@localhost:27017/security_db?authSource=admin"
```

### OptimisticLockException Not Caught
- Ensure `@Transactional` is on service method
- Check that `@Version` field exists in entity
- Verify version is being incremented

### Pessimistic Lock Not Working
- Ensure `@Transactional` is on service method
- Check database supports row-level locking (MySQL InnoDB)
- Verify transaction isolation level

---

## Code Examples

### Complete Flow: Optimistic Locking

```java
// 1. Read entity (gets version = 1)
Product product = repository.findById(1L).get();

// 2. Modify (version still = 1 in memory)
product.setName("New Name");

// 3. Save (checks version in DB)
// If version in DB is still 1 → success, version becomes 2
// If version in DB is 2 → OptimisticLockException
repository.save(product);
```

### Complete Flow: Pessimistic Locking

```java
// 1. Read with lock (SELECT FOR UPDATE)
// Row is locked, other transactions wait
Product product = repository.findByIdWithPessimisticLock(1L).get();

// 2. Modify (row still locked)
product.setName("New Name");

// 3. Save (no conflicts possible - row is locked)
repository.save(product);
// Transaction commits, lock released
```

---

## Performance Considerations

### Optimistic Locking
- ✅ No database locks = better performance
- ✅ Higher concurrency
- ⚠️ Retry logic needed
- ⚠️ Can cause application-level retries

### Pessimistic Locking
- ⚠️ Database locks = lower performance
- ⚠️ Reduced concurrency (transactions wait)
- ✅ No retries needed
- ✅ Guaranteed consistency

---

## Security Note

The locking demo endpoints are configured as public for demonstration purposes. In production:
- Add authentication/authorization
- Implement rate limiting
- Add input validation
- Use HTTPS

---

## References

- [Spring Data JPA Locking](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#locking)
- [Hibernate Locking](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#locking)
- [MongoDB Transactions](https://www.mongodb.com/docs/manual/core/transactions/)
- [Optimistic vs Pessimistic Locking](https://www.baeldung.com/jpa-optimistic-locking)

---

**Happy Locking! 🔒**

