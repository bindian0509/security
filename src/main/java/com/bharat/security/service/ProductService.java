package com.bharat.security.service;

import com.bharat.security.entity.mysql.Product;
import com.bharat.security.repository.mysql.ProductRepository;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service demonstrating Optimistic and Pessimistic Locking with MySQL/JPA.
 */
@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        logger.info("ProductService initialized");
    }

    /**
     * Create a new product (no locking needed for creation).
     */
    @Transactional
    public Product createProduct(Product product) {
        logger.info("Creating product: {}", product.getName());
        Product saved = productRepository.save(product);
        logger.info("Product created with ID: {} and version: {}", saved.getId(), saved.getVersion());
        return saved;
    }

    /**
     * Get product without locking (for read-only operations).
     */
    public Optional<Product> getProduct(Long id) {
        logger.debug("Getting product: {}", id);
        return productRepository.findById(id);
    }

    /**
     * OPTIMISTIC LOCKING EXAMPLE:
     * Updates product using optimistic locking.
     * If another transaction modified the product, version mismatch will cause OptimisticLockException.
     */
    @Transactional
    public Product updateProductOptimistic(Long id, String name, Double price, Integer stock) {
        logger.info("Updating product {} with optimistic locking", id);

        // Read product (gets current version)
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        logger.info("Current version: {}", product.getVersion());

        // Simulate some processing time (increases chance of concurrent modification)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Update fields
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);

        try {
            // Save will check version - if version changed, throws OptimisticLockException
            Product updated = productRepository.save(product);
            logger.info("Product updated successfully. New version: {}", updated.getVersion());
            return updated;
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.warn("Optimistic lock failure! Product was modified by another transaction. Version conflict.");
            throw new OptimisticLockException("Product was modified by another transaction. Please refresh and try again.", e);
        }
    }

    /**
     * PESSIMISTIC LOCKING EXAMPLE:
     * Updates product using pessimistic locking (SELECT FOR UPDATE).
     * Locks the row in database, preventing other transactions from modifying it.
     */
    @Transactional
    public Product updateProductPessimistic(Long id, String name, Double price, Integer stock) {
        logger.info("Updating product {} with pessimistic locking", id);

        // Read with pessimistic write lock (SELECT FOR UPDATE)
        // This locks the row until transaction completes
        Product product = productRepository.findByIdWithPessimisticLock(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        logger.info("Product locked. Current version: {}", product.getVersion());

        // Simulate processing time - other transactions will wait
        try {
            Thread.sleep(2000); // 2 seconds - other transactions must wait
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Update fields
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);

        // Save - no version conflict possible because row is locked
        Product updated = productRepository.save(product);
        logger.info("Product updated with pessimistic lock. New version: {}", updated.getVersion());
        return updated;
    }

    /**
     * Optimistic locking example: Decrease stock (common e-commerce scenario).
     * Multiple users trying to buy last item - optimistic lock prevents overselling.
     */
    @Transactional
    public Product decreaseStockOptimistic(Long id, Integer quantity) {
        logger.info("Decreasing stock for product {} by {} units (optimistic)", id, quantity);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
        }

        // Simulate processing delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        product.setStock(product.getStock() - quantity);

        try {
            return productRepository.save(product);
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.warn("Stock update failed due to concurrent modification. Retry recommended.");
            throw new OptimisticLockException("Product stock was modified. Please refresh and try again.", e);
        }
    }

    /**
     * Pessimistic locking example: Decrease stock.
     * Locks the row, ensuring no concurrent modifications.
     */
    @Transactional
    public Product decreaseStockPessimistic(Long id, Integer quantity) {
        logger.info("Decreasing stock for product {} by {} units (pessimistic)", id, quantity);

        // Lock the row
        Product product = productRepository.findByIdWithPessimisticLock(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
        }

        // Simulate processing - other transactions wait
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        product.setStock(product.getStock() - quantity);
        return productRepository.save(product);
    }

    /**
     * Get all products.
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Delete product.
     */
    @Transactional
    public void deleteProduct(Long id) {
        logger.info("Deleting product: {}", id);
        productRepository.deleteById(id);
    }
}

