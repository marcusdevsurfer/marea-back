package com.devshore.marea.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;
    private String name;
    private double price;
    public Product() {}

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return this.id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    public void setPrice(double price){
        this.price = price;
    }
    public double getPrice() {
        return this.price;
    }
}
