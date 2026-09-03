package com.devshore.marea.controller;

import com.devshore.marea.dto.ProductDTO;
import com.devshore.marea.entity.Product;
import com.devshore.marea.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin("*")
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository productRepository;

    ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping()
    List<Product> getProducts() {
         return productRepository.findAll();
    }
    @PostMapping()
    ResponseEntity<?> createProduct(@RequestBody ProductDTO productDTO) {
        boolean productExist = productRepository.existsByName(productDTO.getName());
        if(productExist) {
            return new ResponseEntity<>("El producto ya existe",HttpStatus.OK);
        }
        Product newProduct = new Product();
        newProduct.setName(productDTO.getName());
        Product productSaved = productRepository.save(newProduct);
        return new ResponseEntity<>(productSaved, HttpStatus.CREATED);
    }
}
