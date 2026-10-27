package com.devshore.marea.controller;

import com.devshore.marea.dto.CreateOrderRequest;
import com.devshore.marea.dto.OrderResponse;
import com.devshore.marea.entity.Order;
import com.devshore.marea.entity.OrderItem;
import com.devshore.marea.entity.Product;
import com.devshore.marea.repository.OrderRepository;
import com.devshore.marea.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderController(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }


    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderResponse> responseList = new ArrayList<>();

        for (Order order : orders) {
            List<OrderResponse.Item> responseItems = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                responseItems.add(new OrderResponse.Item(
                        item.getProduct().getId(), item.getProduct().getName(),
                        item.getQuantity(), item.getUnitPrice()));
            }
            responseList.add(new OrderResponse(order.getId(), order.getOwner(), responseItems, order.getTotalPrice()));
        }

        return ResponseEntity.ok(responseList);
    }
    @PostMapping
    @Transactional
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        if (request.getOwner() == null || request.getOwner().isBlank()
                || request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("La orden requiere un propietario y al menos un producto");
        }

        Order order = new Order();
        order.setOwner(request.getOwner());
        List<OrderResponse.Item> responseItems = new ArrayList<>();

        for (CreateOrderRequest.Item requestedItem : request.getItems()) {
            if (requestedItem == null || requestedItem.getProductId() == null || requestedItem.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body("Cada producto requiere un id y una cantidad mayor que cero");
            }

            Product product = productRepository.findById(requestedItem.getProductId()).orElse(null);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe el producto " + requestedItem.getProductId());
            }

            OrderItem item = new OrderItem(product, requestedItem.getQuantity());
            order.addItem(item);
            responseItems.add(new OrderResponse.Item(
                    product.getId(), product.getName(), item.getQuantity(), item.getUnitPrice()));
        }

        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(new OrderResponse(
                savedOrder.getId(), savedOrder.getOwner(), responseItems, savedOrder.getTotalPrice()));
    }
}
