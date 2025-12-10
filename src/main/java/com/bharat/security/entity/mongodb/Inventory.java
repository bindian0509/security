package com.bharat.security.entity.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB Document demonstrating Optimistic and Pessimistic Locking.
 *
 * Optimistic Locking: Uses @Version annotation
 * Pessimistic Locking: Applied using MongoDB transactions with write concern
 */
@Document(collection = "inventory")
public class Inventory {

    @Id
    private String id;

    private String itemName;
    private String category;
    private Double price;
    private Integer quantity;

    /**
     * Version field for Optimistic Locking in MongoDB.
     * Automatically incremented on each update.
     * Prevents lost updates when multiple transactions modify the same document.
     */
    @Version
    private Long version;

    // Constructors
    public Inventory() {
    }

    public Inventory(String itemName, String category, Double price, Integer quantity) {
        this.itemName = itemName;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "id='" + id + '\'' +
                ", itemName='" + itemName + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", version=" + version +
                '}';
    }
}

