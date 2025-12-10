package com.bharat.security.entity.mysql;

import jakarta.persistence.*;
import org.hibernate.annotations.OptimisticLocking;

/**
 * MySQL JPA Entity demonstrating Optimistic and Pessimistic Locking.
 *
 * Optimistic Locking: Uses @Version annotation
 * Pessimistic Locking: Applied at query level using LockModeType
 */
@Entity
@Table(name = "products")
@OptimisticLocking(type = org.hibernate.annotations.OptimisticLockType.VERSION)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer stock;

    /**
     * Version field for Optimistic Locking.
     * Automatically incremented on each update.
     * Prevents lost updates when multiple transactions modify the same record.
     */
    @Version
    @Column(name = "version")
    private Long version;

    // Constructors
    public Product() {
    }

    public Product(String name, String description, Double price, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", version=" + version +
                '}';
    }
}

