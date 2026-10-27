package com.devshore.marea.controller;

import com.devshore.marea.dto.CreateOrderRequest;
import com.devshore.marea.dto.OrderResponse;
import com.devshore.marea.entity.Order;
import com.devshore.marea.entity.OrderItem;
import com.devshore.marea.entity.Product;
import com.devshore.marea.repository.OrderRepository;
import com.devshore.marea.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class OrderControllerTest {
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final OrderController controller = new OrderController(orderRepository, productRepository);

    @Test
    void returnsAllOrdersWithTheirItemsAndTotals() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Product product = product(productId, "Cafe", 3.5);
        Order order = new Order();
        order.setId(orderId);
        order.setOwner("Ana");
        order.addItem(new OrderItem(product, 2));
        when(orderRepository.findAll()).thenReturn(List.of(order));

        ResponseEntity<List<OrderResponse>> response = controller.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(new OrderResponse(
                orderId,
                "Ana",
                List.of(new OrderResponse.Item(productId, "Cafe", 2, 3.5)),
                7.0)), response.getBody());
        verify(orderRepository).findAll();
    }

    @Test
    void returnsEmptyListWhenThereAreNoOrders() {
        when(orderRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<OrderResponse>> response = controller.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(), response.getBody());
        verify(orderRepository).findAll();
    }

    @Test
    void rejectsMissingOwnerAndMissingOrEmptyItems() {
        CreateOrderRequest missingOwner = request(null, validItem(UUID.randomUUID(), 1));
        CreateOrderRequest missingItems = request("Ana", null);
        CreateOrderRequest emptyItems = request("Ana");

        assertBadRequest(missingOwner, "La orden requiere un propietario y al menos un producto");
        assertBadRequest(missingItems, "La orden requiere un propietario y al menos un producto");
        assertBadRequest(emptyItems, "La orden requiere un propietario y al menos un producto");
        verifyNoInteractions(orderRepository, productRepository);
    }

    @Test
    void rejectsBlankOwner() {
        ResponseEntity<?> response = controller.createOrder(request("  ", validItem(UUID.randomUUID(), 1)));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(orderRepository, productRepository);
    }

    @Test
    void rejectsInvalidItems() {
        UUID productId = UUID.randomUUID();
        assertBadRequest(request("Ana", (CreateOrderRequest.Item) null),
                "Cada producto requiere un id y una cantidad mayor que cero");
        assertBadRequest(request("Ana", validItem(null, 1)),
                "Cada producto requiere un id y una cantidad mayor que cero");
        assertBadRequest(request("Ana", validItem(productId, 0)),
                "Cada producto requiere un id y una cantidad mayor que cero");
        assertBadRequest(request("Ana", validItem(productId, -1)),
                "Cada producto requiere un id y una cantidad mayor que cero");
        verifyNoInteractions(orderRepository, productRepository);
    }

    @Test
    void returnsNotFoundWhenProductDoesNotExist() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.createOrder(request("Ana", validItem(productId, 1)));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No existe el producto " + productId, response.getBody());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createsOrderWithItemsAndCalculatedTotal() {
        UUID firstProductId = UUID.randomUUID();
        UUID secondProductId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Product firstProduct = product(firstProductId, "Cafe", 3.5);
        Product secondProduct = product(secondProductId, "Te", 2.0);
        when(productRepository.findById(firstProductId)).thenReturn(Optional.of(firstProduct));
        when(productRepository.findById(secondProductId)).thenReturn(Optional.of(secondProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(orderId);
            return order;
        });

        ResponseEntity<?> response = controller.createOrder(request(
                "Ana",
                validItem(firstProductId, 2),
                validItem(secondProductId, 3)));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        OrderResponse body = assertInstanceOf(OrderResponse.class, response.getBody());
        assertEquals(orderId, body.id());
        assertEquals("Ana", body.owner());
        assertEquals(13.0, body.totalPrice());
        assertEquals(List.of(
                new OrderResponse.Item(firstProductId, "Cafe", 2, 3.5),
                new OrderResponse.Item(secondProductId, "Te", 3, 2.0)), body.items());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void doesNotSaveOrderWhenAnyProductIsMissing() {
        UUID existingProductId = UUID.randomUUID();
        UUID missingProductId = UUID.randomUUID();
        when(productRepository.findById(existingProductId))
                .thenReturn(Optional.of(product(existingProductId, "Cafe", 3.5)));
        when(productRepository.findById(missingProductId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.createOrder(request(
                "Ana",
                validItem(existingProductId, 1),
                validItem(missingProductId, 1)));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(orderRepository, never()).save(any());
    }

    private void assertBadRequest(CreateOrderRequest request, String message) {
        ResponseEntity<?> response = controller.createOrder(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(message, response.getBody());
        assertNull(response.getHeaders().getLocation());
    }

    private static CreateOrderRequest request(String owner, CreateOrderRequest.Item... items) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOwner(owner);
        request.setItems(items == null ? null : Arrays.asList(items));
        return request;
    }

    private static CreateOrderRequest.Item validItem(UUID productId, int quantity) {
        CreateOrderRequest.Item item = new CreateOrderRequest.Item();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private static Product product(UUID id, String name, double price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        return product;
    }
}
