package com.natasatm.photo_gallery.web;

import com.natasatm.photo_gallery.dto.CreateOrUpdatePriceListRequest;
import com.natasatm.photo_gallery.dto.PriceListDto;
import com.natasatm.photo_gallery.model.Currency;
import com.natasatm.photo_gallery.model.PriceList;
import com.natasatm.photo_gallery.repository.CurrencyRepository;
import com.natasatm.photo_gallery.repository.PriceListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * @author Natasa Todorov Markovic
 */
@RestController
@RequestMapping("/api/price-lists")
@RequiredArgsConstructor
public class PriceListController {

    private final PriceListRepository priceListRepository;
    private final CurrencyRepository currencyRepository;

    @GetMapping
    public List<PriceListDto> getAll() {
        return priceListRepository.findAllByOrderByNameAsc().stream()
                .map(PriceListDto::fromEntity)
                .toList();
    }

    @PostMapping
    public PriceListDto create(@RequestBody CreateOrUpdatePriceListRequest request) {
        Currency currency = currencyRepository.findByCode(request.getCurrencyCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown currency"));

        PriceList pl = PriceList.builder()
                .name(request.getName())
                .currency(currency)
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();

        PriceList saved = priceListRepository.save(pl);
        return PriceListDto.fromEntity(saved);
    }

    @PutMapping("/update/{id}")
    public PriceListDto update(@PathVariable Long id,
                               @RequestBody CreateOrUpdatePriceListRequest request) {
        PriceList pl = priceListRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price list not found"));

        Currency currency = currencyRepository.findByCode(request.getCurrencyCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown currency"));

        pl.setName(request.getName());
        pl.setCurrency(currency);
        pl.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        PriceList saved = priceListRepository.save(pl);
        return PriceListDto.fromEntity(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        // za PriceList za sada može i hard delete
        priceListRepository.deleteById(id);
    }
}
