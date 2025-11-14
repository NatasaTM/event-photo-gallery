package com.natasatm.photo_gallery.web;

import com.natasatm.photo_gallery.dto.CreateOrUpdatePriceRequest;
import com.natasatm.photo_gallery.dto.PriceDto;
import com.natasatm.photo_gallery.model.Price;
import com.natasatm.photo_gallery.model.PriceList;
import com.natasatm.photo_gallery.model.Product;
import com.natasatm.photo_gallery.repository.PriceListRepository;
import com.natasatm.photo_gallery.repository.PriceRepository;
import com.natasatm.photo_gallery.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * @author Natasa Todorov Markovic
 */
@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceRepository priceRepository;
    private final ProductRepository productRepository;
    private final PriceListRepository priceListRepository;

    @GetMapping("/by-price-list/{priceListId}")
    public List<PriceDto> getByPriceList(@PathVariable Long priceListId) {
        return priceRepository.findByPriceListIdOrderByProduct_NameAsc(priceListId)
                .stream()
                .map(PriceDto::fromEntity)
                .toList();
    }

    @PostMapping
    public PriceDto create(@RequestBody CreateOrUpdatePriceRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));

        PriceList priceList = priceListRepository.findById(request.getPriceListId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price list not found"));

        Price price = Price.builder()
                .product(product)
                .priceList(priceList)
                .priceAmount(request.getPriceAmount())
                .taxRate(request.getTaxRate() != null ? request.getTaxRate() : 0)
                .minQty(request.getMinQty() != null ? request.getMinQty() : 1)
                .build();

        Price saved = priceRepository.save(price);
        return PriceDto.fromEntity(saved);
    }

    @PutMapping("/update/{id}")
    public PriceDto update(@PathVariable Long id,
                           @RequestBody CreateOrUpdatePriceRequest request) {
        Price price = priceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));

        PriceList priceList = priceListRepository.findById(request.getPriceListId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price list not found"));

        price.setProduct(product);
        price.setPriceList(priceList);
        price.setPriceAmount(request.getPriceAmount());
        price.setTaxRate(request.getTaxRate() != null ? request.getTaxRate() : 0);
        price.setMinQty(request.getMinQty() != null ? request.getMinQty() : 1);

        Price saved = priceRepository.save(price);
        return PriceDto.fromEntity(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        priceRepository.deleteById(id);
    }
}
