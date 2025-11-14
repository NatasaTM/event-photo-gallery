package com.natasatm.photo_gallery.web;

import com.natasatm.photo_gallery.dto.EventPriceListDto;
import com.natasatm.photo_gallery.dto.LinkPriceListToEventRequest;
import com.natasatm.photo_gallery.model.Event;
import com.natasatm.photo_gallery.model.EventPriceList;
import com.natasatm.photo_gallery.model.PriceList;
import com.natasatm.photo_gallery.repository.EventPriceListRepository;
import com.natasatm.photo_gallery.repository.EventRepository;
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
@RequiredArgsConstructor
public class EventPriceListController {

    private final EventRepository eventRepository;
    private final PriceListRepository priceListRepository;
    private final EventPriceListRepository eventPriceListRepository;

    // Sve važeće veze za jedan event
    @GetMapping("/api/events/{eventId}/price-lists")
    public List<EventPriceListDto> getForEvent(@PathVariable Long eventId) {
        return eventPriceListRepository
                .findByEventIdAndActiveTrueOrderByPriorityDesc(eventId)
                .stream()
                .map(EventPriceListDto::fromEntity)
                .toList();
    }

    // Kreiranje nove veze event–cenovnik
    @PostMapping("/api/events/{eventId}/price-lists")
    public EventPriceListDto linkPriceList(@PathVariable Long eventId,
                                           @RequestBody LinkPriceListToEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        PriceList priceList = priceListRepository.findById(request.getPriceListId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "PriceList not found"));

        // (Opcija: ovde možeš da proveriš da li valuta cenovnika odgovara
        // primary/secondary valuti eventa)

        EventPriceList epl = EventPriceList.builder()
                .event(event)
                .priceList(priceList)
                .active(true)
                .defaultForCurrency(Boolean.TRUE.equals(request.getDefaultForCurrency()))
                .priority(0) // za sada ne koristimo
                .build();

        EventPriceList saved = eventPriceListRepository.save(epl);

        // ako je defaultForCurrency = true, očisti prethodne za istu valutu
        if (saved.getDefaultForCurrency()) {
            String currencyCode = priceList.getCurrency().getCode();
            clearOtherDefaults(eventId, currencyCode, saved.getId());
        }

        return EventPriceListDto.fromEntity(saved);
    }

    // helper
    private void clearOtherDefaults(Long eventId, String currencyCode, Long keepId) {
        List<EventPriceList> list = eventPriceListRepository
                .findByEventIdAndActiveTrueOrderByPriorityDesc(eventId);

        for (EventPriceList e : list) {
            if (!e.getId().equals(keepId) &&
                    e.getPriceList().getCurrency().getCode().equals(currencyCode) &&
                    Boolean.TRUE.equals(e.getDefaultForCurrency())) {
                e.setDefaultForCurrency(false);
                eventPriceListRepository.save(e);
            }
        }
    }

    // Postavi kao default (za tu valutu)
    @PutMapping("/api/event-price-lists/{id}/set-default")
    public EventPriceListDto setDefault(@PathVariable Long id) {
        EventPriceList epl = eventPriceListRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "EventPriceList not found"));

        Event event = epl.getEvent();
        String currencyCode = epl.getPriceList().getCurrency().getCode();

        // očisti druge
        clearOtherDefaults(event.getId(), currencyCode, epl.getId());

        epl.setDefaultForCurrency(true);
        EventPriceList saved = eventPriceListRepository.save(epl);
        return EventPriceListDto.fromEntity(saved);
    }

    // "Ukloni" – soft delete
    @DeleteMapping("/api/event-price-lists/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        EventPriceList epl = eventPriceListRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "EventPriceList not found"));

        epl.setActive(false);
        eventPriceListRepository.save(epl);
    }
}
