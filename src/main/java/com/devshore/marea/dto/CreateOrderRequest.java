package com.devshore.marea.dto;

import java.util.List;
import java.util.UUID;

public class CreateOrderRequest {
    private String owner;
    private List<Item> items;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static class Item {
        private UUID productId;
        private int quantity;

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        public UUID getProductId() {
            return productId;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
