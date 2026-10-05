package com.devshore.marea.dto;


public class ProductDTO {
    public String name;
    public double price;

    public ProductDTO(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }

    public void setPrice(double price){this.price = price;}
    public double getPrice(){return this.price;}

}
