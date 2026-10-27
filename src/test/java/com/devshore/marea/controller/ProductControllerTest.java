package com.devshore.marea.controller;

import com.devshore.marea.dto.ProductDTO;
import com.devshore.marea.entity.Product;
import com.devshore.marea.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductControllerTest {
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final ProductController controller = new ProductController(productRepository);

    @Test
    void returnsAllProducts() {
        List<Product> products = List.of(product("Cafe", 3.5), product("Te", 2.0));
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = controller.getProducts();

        assertEquals(products, result);
        verify(productRepository).findAll();
    }

    @Test
    void createsProductWhenNameDoesNotExist() {
        when(productRepository.existsByName("Cafe")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = controller.createProduct(new ProductDTO("Cafe", 3.5));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ProductDTO body = assertInstanceOf(ProductDTO.class, response.getBody());
        assertEquals("Cafe", body.getName());
        assertEquals(3.5, body.getPrice());
        verify(productRepository).existsByName("Cafe");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void doesNotCreateDuplicateProduct() {
        when(productRepository.existsByName("Cafe")).thenReturn(true);

        ResponseEntity<?> response = controller.createProduct(new ProductDTO("Cafe", 3.5));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("El producto ya existe", response.getBody());
        verify(productRepository).existsByName("Cafe");
        verify(productRepository, never()).save(any(Product.class));
    }

    private static Product product(String name, double price) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        return product;
    }
}
