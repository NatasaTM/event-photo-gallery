package com.natasatm.photo_gallery.web;

import com.natasatm.photo_gallery.dto.CreateProductTypeRequest;
import com.natasatm.photo_gallery.dto.ProductTypeDto;
import com.natasatm.photo_gallery.model.ProductType;
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
@RequestMapping("/api/product-types")
@RequiredArgsConstructor
public class ProductTypeController {

    private final ProductTypeRepository productTypeRepository;

    @GetMapping
    public List<ProductTypeDto> getAll() {
        return productTypeRepository.findAllByOrderByNameAsc()
                .stream()
                .map(ProductTypeDto::fromEntity)
                .toList();
    }

    @PostMapping
    public ProductTypeDto create(@RequestBody CreateProductTypeRequest request) {
        // opciono: mala validacija jedinstvenog imena
        productTypeRepository.findByName(request.getName())
                .ifPresent(pt -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Product type already exists");
                });

        ProductType pt = ProductType.builder()
                .name(request.getName())
                .build();

        ProductType saved = productTypeRepository.save(pt);
        return ProductTypeDto.fromEntity(saved);
    }
}
