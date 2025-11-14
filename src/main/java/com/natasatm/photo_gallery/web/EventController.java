package com.natasatm.photo_gallery.web;

import com.natasatm.photo_gallery.dto.CreateEventRequest;
import com.natasatm.photo_gallery.dto.EventDto;
import com.natasatm.photo_gallery.dto.UpdateEventRequest;
import com.natasatm.photo_gallery.model.Currency;
import com.natasatm.photo_gallery.model.Event;
import com.natasatm.photo_gallery.repository.CurrencyRepository;
import com.natasatm.photo_gallery.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * @author Natasa Todorov Markovic
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final CurrencyRepository currencyRepository;

    @GetMapping("/all")
    public List<EventDto> getAll() {
        return eventRepository.findByActiveTrueOrderByDateDesc().stream()
                .map(EventDto::fromEntity)
                .toList();
    }

    @PostMapping("/create")
    public EventDto create(@RequestBody CreateEventRequest request) {
        Currency primary = currencyRepository.findByCode(request.getPrimaryCurrencyCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown primary currency: " + request.getPrimaryCurrencyCode()
                ));

        Currency secondary = null;
        if (request.getSecondaryCurrencyCode() != null && !request.getSecondaryCurrencyCode().isBlank()) {
            secondary = currencyRepository.findByCode(request.getSecondaryCurrencyCode())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown secondary currency: " + request.getSecondaryCurrencyCode()
                    ));
        }

        Event event = Event.builder()
                .name(request.getName())
                .place(request.getPlace())
                .date(request.getDate())
                .primaryCurrency(primary)
                .secondaryCurrency(secondary)
                .active(true)
                .build();

        Event saved = eventRepository.save(event);
        return EventDto.fromEntity(saved);
    }

    @PutMapping("/{id}")
    public EventDto update(@PathVariable Long id,
                           @RequestBody UpdateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        event.setName(request.getName());
        event.setPlace(request.getPlace());
        event.setDate(request.getDate());

        Currency primary = currencyRepository.findByCode(request.getPrimaryCurrencyCode())
                .orElseThrow(() -> new IllegalArgumentException("Unknown primary currency"));
        event.setPrimaryCurrency(primary);

        if (request.getSecondaryCurrencyCode() != null && !request.getSecondaryCurrencyCode().isBlank()) {
            Currency secondary = currencyRepository.findByCode(request.getSecondaryCurrencyCode())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown secondary currency"));
            event.setSecondaryCurrency(secondary);
        } else {
            event.setSecondaryCurrency(null);
        }

        Event saved = eventRepository.save(event);
        return EventDto.fromEntity(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(@PathVariable Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        event.setActive(false);
        eventRepository.save(event);
    }

}
