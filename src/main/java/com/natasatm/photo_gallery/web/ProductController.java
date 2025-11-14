package com.natasatm.photo_gallery.web;

import com.natasatm.photo_gallery.dto.CreateOrUpdateProductRequest;
import com.natasatm.photo_gallery.dto.ProductDto;
import com.natasatm.photo_gallery.model.Product;
import com.natasatm.photo_gallery.model.ProductType;
import com.natasatm.photo_gallery.repository.ProductRepository;
import com.natasatm.photo_gallery.repository.ProductTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * @author Natasa Todorov Markovic
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;

    @GetMapping
    public List<ProductDto> getAllActive() {
        return productRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(ProductDto::fromEntity)
                .toList();
    }

    @PostMapping
    public ProductDto create(@RequestBody CreateOrUpdateProductRequest request) {
        ProductType type = productTypeRepository.findById(request.getProductTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product type not found"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .productType(type)
                .isActive(true)
                .build();

        Product saved = productRepository.save(product);
        return ProductDto.fromEntity(saved);
    }

    @PutMapping("/update/{id}")
    public ProductDto update(@PathVariable Long id,
                             @RequestBody CreateOrUpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        ProductType type = productTypeRepository.findById(request.getProductTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product type not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setProductType(type);

        Product saved = productRepository.save(product);
        return ProductDto.fromEntity(saved);
    }

    @DeleteMapping("/archive/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        product.setIsActive(false);
        productRepository.save(product);
    }
}
