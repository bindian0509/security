package com.bharat.security.repository.mongodb;

import com.bharat.security.entity.mongodb.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB Repository demonstrating Optimistic and Pessimistic Locking.
 */
@Repository
public interface InventoryRepository extends MongoRepository<Inventory, String> {

    /**
     * Optimistic Locking: Uses @Version field automatically.
     * If version doesn't match, throws OptimisticLockingFailureException.
     */
    Optional<Inventory> findById(String id);

    /**
     * Find by item name (for testing)
     */
    Optional<Inventory> findByItemName(String itemName);
}

