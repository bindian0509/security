package com.bharat.security.service;

import com.bharat.security.entity.mongodb.Inventory;
import com.bharat.security.repository.mongodb.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service demonstrating Optimistic and Pessimistic Locking with MongoDB.
 */
@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryRepository inventoryRepository;
    private final MongoTemplate mongoTemplate;

    public InventoryService(InventoryRepository inventoryRepository, MongoTemplate mongoTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.mongoTemplate = mongoTemplate;
        logger.info("InventoryService initialized");
    }

    /**
     * Create a new inventory item.
     */
    @Transactional
    public Inventory createInventory(Inventory inventory) {
        logger.info("Creating inventory item: {}", inventory.getItemName());
        Inventory saved = inventoryRepository.save(inventory);
        logger.info("Inventory created with ID: {} and version: {}", saved.getId(), saved.getVersion());
        return saved;
    }

    /**
     * Get inventory item without locking.
     */
    public Optional<Inventory> getInventory(String id) {
        logger.debug("Getting inventory: {}", id);
        return inventoryRepository.findById(id);
    }

    /**
     * OPTIMISTIC LOCKING EXAMPLE:
     * Updates inventory using optimistic locking with @Version.
     */
    @Transactional
    public Inventory updateInventoryOptimistic(String id, String itemName, Double price, Integer quantity) {
        logger.info("Updating inventory {} with optimistic locking", id);

        // Read inventory (gets current version)
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found: " + id));

        logger.info("Current version: {}", inventory.getVersion());

        // Simulate processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Update fields
        inventory.setItemName(itemName);
        inventory.setPrice(price);
        inventory.setQuantity(quantity);

        try {
            // Save will check version - if version changed, throws OptimisticLockingFailureException
            Inventory updated = inventoryRepository.save(inventory);
            logger.info("Inventory updated successfully. New version: {}", updated.getVersion());
            return updated;
        } catch (OptimisticLockingFailureException e) {
            logger.warn("Optimistic lock failure! Inventory was modified by another transaction.");
            throw new RuntimeException("Inventory was modified by another transaction. Please refresh and try again.", e);
        }
    }

    /**
     * PESSIMISTIC LOCKING EXAMPLE:
     * Uses MongoDB transactions with write concern to achieve pessimistic locking behavior.
     * In MongoDB, we use transactions to lock documents.
     */
    @Transactional
    public Inventory updateInventoryPessimistic(String id, String itemName, Double price, Integer quantity) {
        logger.info("Updating inventory {} with pessimistic locking (MongoDB transaction)", id);

        // Use MongoTemplate with transaction to lock the document
        Query query = new Query(Criteria.where("_id").is(id));

        // Read with transaction (locks document)
        Inventory inventory = mongoTemplate.findOne(query, Inventory.class);
        if (inventory == null) {
            throw new RuntimeException("Inventory not found: " + id);
        }

        logger.info("Inventory locked in transaction. Current version: {}", inventory.getVersion());

        // Simulate processing - transaction holds lock
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Update within transaction
        Update update = new Update()
                .set("itemName", itemName)
                .set("price", price)
                .set("quantity", quantity);

        mongoTemplate.updateFirst(query, update, Inventory.class);

        // Reload to get updated version
        Inventory updated = mongoTemplate.findOne(query, Inventory.class);
        logger.info("Inventory updated with pessimistic lock. New version: {}", updated.getVersion());
        return updated;
    }

    /**
     * Optimistic locking: Decrease quantity.
     */
    @Transactional
    public Inventory decreaseQuantityOptimistic(String id, Integer quantity) {
        logger.info("Decreasing quantity for inventory {} by {} units (optimistic)", id, quantity);

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found: " + id));

        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient quantity. Available: " + inventory.getQuantity());
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);

        try {
            return inventoryRepository.save(inventory);
        } catch (OptimisticLockingFailureException e) {
            logger.warn("Quantity update failed due to concurrent modification.");
            throw new RuntimeException("Inventory was modified. Please refresh and try again.", e);
        }
    }

    /**
     * Pessimistic locking: Decrease quantity using MongoDB transaction.
     */
    @Transactional
    public Inventory decreaseQuantityPessimistic(String id, Integer quantity) {
        logger.info("Decreasing quantity for inventory {} by {} units (pessimistic)", id, quantity);

        Query query = new Query(Criteria.where("_id").is(id));
        Inventory inventory = mongoTemplate.findOne(query, Inventory.class);

        if (inventory == null) {
            throw new RuntimeException("Inventory not found: " + id);
        }

        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient quantity. Available: " + inventory.getQuantity());
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Update update = new Update().inc("quantity", -quantity);
        mongoTemplate.updateFirst(query, update, Inventory.class);

        return mongoTemplate.findOne(query, Inventory.class);
    }

    /**
     * Get all inventory items.
     */
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    /**
     * Delete inventory item.
     */
    @Transactional
    public void deleteInventory(String id) {
        logger.info("Deleting inventory: {}", id);
        inventoryRepository.deleteById(id);
    }
}

