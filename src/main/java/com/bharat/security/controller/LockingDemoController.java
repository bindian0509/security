package com.bharat.security.controller;

import com.bharat.security.entity.mongodb.Inventory;
import com.bharat.security.entity.mysql.Product;
import com.bharat.security.service.InventoryService;
import com.bharat.security.service.ProductService;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller demonstrating Optimistic and Pessimistic Locking with MySQL and MongoDB.
 */
@RestController
@RequestMapping("/api/locking")
public class LockingDemoController {

    private static final Logger logger = LoggerFactory.getLogger(LockingDemoController.class);
    private final ProductService productService;
    private final InventoryService inventoryService;

    public LockingDemoController(ProductService productService, InventoryService inventoryService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
        logger.info("LockingDemoController initialized");
    }

    // ==================== MySQL Product Endpoints ====================

    /**
     * Create a new product (MySQL).
     */
    @PostMapping("/mysql/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        logger.info("Creating product: {}", product.getName());
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get product by ID (MySQL - no locking).
     */
    @GetMapping("/mysql/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.getProduct(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all products (MySQL).
     */
    @GetMapping("/mysql/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Update product with OPTIMISTIC LOCKING (MySQL).
     *
     * Test concurrent updates:
     * 1. Call this endpoint twice simultaneously with different data
     * 2. One will succeed, other will get OptimisticLockException
     */
    @PutMapping("/mysql/products/{id}/optimistic")
    public ResponseEntity<?> updateProductOptimistic(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        try {
            String name = (String) updates.getOrDefault("name", "");
            Double price = updates.get("price") != null ?
                Double.valueOf(updates.get("price").toString()) : null;
            Integer stock = updates.get("stock") != null ?
                Integer.valueOf(updates.get("stock").toString()) : null;

            Product updated = productService.updateProductOptimistic(id, name, price, stock);
            return ResponseEntity.ok(Map.of(
                "message", "Product updated successfully with optimistic locking",
                "product", updated,
                "version", updated.getVersion()
            ));
        } catch (OptimisticLockException e) {
            logger.warn("Optimistic lock exception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "OptimisticLockException",
                "message", "Product was modified by another transaction. Please refresh and try again.",
                "details", e.getMessage()
            ));
        }
    }

    /**
     * Update product with PESSIMISTIC LOCKING (MySQL).
     *
     * Test concurrent updates:
     * 1. Call this endpoint twice simultaneously
     * 2. Second request will wait until first completes (SELECT FOR UPDATE)
     */
    @PutMapping("/mysql/products/{id}/pessimistic")
    public ResponseEntity<?> updateProductPessimistic(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        try {
            String name = (String) updates.getOrDefault("name", "");
            Double price = updates.get("price") != null ?
                Double.valueOf(updates.get("price").toString()) : null;
            Integer stock = updates.get("stock") != null ?
                Integer.valueOf(updates.get("stock").toString()) : null;

            Product updated = productService.updateProductPessimistic(id, name, price, stock);
            return ResponseEntity.ok(Map.of(
                "message", "Product updated successfully with pessimistic locking",
                "product", updated,
                "version", updated.getVersion()
            ));
        } catch (Exception e) {
            logger.error("Error updating product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Update failed",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Decrease stock with OPTIMISTIC LOCKING (MySQL).
     * Simulates e-commerce scenario - multiple users buying same item.
     */
    @PostMapping("/mysql/products/{id}/decrease-stock/optimistic")
    public ResponseEntity<?> decreaseStockOptimistic(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer quantity = request.get("quantity");
            Product updated = productService.decreaseStockOptimistic(id, quantity);
            return ResponseEntity.ok(Map.of(
                "message", "Stock decreased successfully (optimistic locking)",
                "product", updated,
                "remainingStock", updated.getStock()
            ));
        } catch (OptimisticLockException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "OptimisticLockException",
                "message", "Product stock was modified. Please refresh and try again."
            ));
        }
    }

    /**
     * Decrease stock with PESSIMISTIC LOCKING (MySQL).
     */
    @PostMapping("/mysql/products/{id}/decrease-stock/pessimistic")
    public ResponseEntity<?> decreaseStockPessimistic(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer quantity = request.get("quantity");
            Product updated = productService.decreaseStockPessimistic(id, quantity);
            return ResponseEntity.ok(Map.of(
                "message", "Stock decreased successfully (pessimistic locking)",
                "product", updated,
                "remainingStock", updated.getStock()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Update failed",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete product (MySQL).
     */
    @DeleteMapping("/mysql/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }

    // ==================== MongoDB Inventory Endpoints ====================

    /**
     * Create a new inventory item (MongoDB).
     */
    @PostMapping("/mongodb/inventory")
    public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {
        logger.info("Creating inventory item: {}", inventory.getItemName());
        Inventory created = inventoryService.createInventory(inventory);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get inventory by ID (MongoDB - no locking).
     */
    @GetMapping("/mongodb/inventory/{id}")
    public ResponseEntity<Inventory> getInventory(@PathVariable String id) {
        return inventoryService.getInventory(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all inventory items (MongoDB).
     */
    @GetMapping("/mongodb/inventory")
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    /**
     * Update inventory with OPTIMISTIC LOCKING (MongoDB).
     */
    @PutMapping("/mongodb/inventory/{id}/optimistic")
    public ResponseEntity<?> updateInventoryOptimistic(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        try {
            String itemName = (String) updates.getOrDefault("itemName", "");
            Double price = updates.get("price") != null ?
                Double.valueOf(updates.get("price").toString()) : null;
            Integer quantity = updates.get("quantity") != null ?
                Integer.valueOf(updates.get("quantity").toString()) : null;

            Inventory updated = inventoryService.updateInventoryOptimistic(id, itemName, price, quantity);
            return ResponseEntity.ok(Map.of(
                "message", "Inventory updated successfully with optimistic locking",
                "inventory", updated,
                "version", updated.getVersion()
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("modified by another transaction")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "OptimisticLockingFailureException",
                    "message", "Inventory was modified by another transaction. Please refresh and try again."
                ));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Update failed",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Update inventory with PESSIMISTIC LOCKING (MongoDB).
     */
    @PutMapping("/mongodb/inventory/{id}/pessimistic")
    public ResponseEntity<?> updateInventoryPessimistic(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        try {
            String itemName = (String) updates.getOrDefault("itemName", "");
            Double price = updates.get("price") != null ?
                Double.valueOf(updates.get("price").toString()) : null;
            Integer quantity = updates.get("quantity") != null ?
                Integer.valueOf(updates.get("quantity").toString()) : null;

            Inventory updated = inventoryService.updateInventoryPessimistic(id, itemName, price, quantity);
            return ResponseEntity.ok(Map.of(
                "message", "Inventory updated successfully with pessimistic locking",
                "inventory", updated,
                "version", updated.getVersion()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Update failed",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Decrease quantity with OPTIMISTIC LOCKING (MongoDB).
     */
    @PostMapping("/mongodb/inventory/{id}/decrease-quantity/optimistic")
    public ResponseEntity<?> decreaseQuantityOptimistic(
            @PathVariable String id,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer quantity = request.get("quantity");
            Inventory updated = inventoryService.decreaseQuantityOptimistic(id, quantity);
            return ResponseEntity.ok(Map.of(
                "message", "Quantity decreased successfully (optimistic locking)",
                "inventory", updated,
                "remainingQuantity", updated.getQuantity()
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("modified")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "OptimisticLockingFailureException",
                    "message", "Inventory was modified. Please refresh and try again."
                ));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Update failed",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Decrease quantity with PESSIMISTIC LOCKING (MongoDB).
     */
    @PostMapping("/mongodb/inventory/{id}/decrease-quantity/pessimistic")
    public ResponseEntity<?> decreaseQuantityPessimistic(
            @PathVariable String id,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer quantity = request.get("quantity");
            Inventory updated = inventoryService.decreaseQuantityPessimistic(id, quantity);
            return ResponseEntity.ok(Map.of(
                "message", "Quantity decreased successfully (pessimistic locking)",
                "inventory", updated,
                "remainingQuantity", updated.getQuantity()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Update failed",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete inventory item (MongoDB).
     */
    @DeleteMapping("/mongodb/inventory/{id}")
    public ResponseEntity<?> deleteInventory(@PathVariable String id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.ok(Map.of("message", "Inventory deleted successfully"));
    }
}

