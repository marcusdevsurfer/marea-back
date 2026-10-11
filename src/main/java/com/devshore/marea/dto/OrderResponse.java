package com.devshore.marea.dto;

import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String owner,
        List<Item> items,
        double totalPrice
) {
    public record Item(UUID productId, String productName, int quantity, double unitPrice) {
    }
}
