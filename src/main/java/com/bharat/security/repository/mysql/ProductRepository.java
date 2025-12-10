package com.bharat.security.repository.mysql;

import com.bharat.security.entity.mysql.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MySQL JPA Repository demonstrating Optimistic and Pessimistic Locking.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Optimistic Locking: Uses @Version field automatically.
     * If version doesn't match, throws OptimisticLockException.
     */
    Optional<Product> findById(Long id);

    /**
     * Pessimistic Locking: Uses SELECT FOR UPDATE.
     * Locks the row in database until transaction completes.
     * Other transactions must wait.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithPessimisticLock(@Param("id") Long id);

    /**
     * Pessimistic Read Lock: Allows concurrent reads but blocks writes.
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithPessimisticReadLock(@Param("id") Long id);

    /**
     * Optimistic Force Increment: Increments version even if entity not modified.
     */
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithOptimisticForceIncrement(@Param("id") Long id);
}

