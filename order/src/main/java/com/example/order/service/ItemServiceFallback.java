package com.example.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.order.dto.ItemDTO;
import com.example.order.exceptions.ItemNotFoundException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class ItemServiceFallback {

    private final ItemRetryService itemRetryService;

    private static final Logger log = LoggerFactory.getLogger(ItemServiceFallback.class);

    public ItemServiceFallback(ItemRetryService itemRetryService) {
        this.itemRetryService = itemRetryService;
    }

    @CircuitBreaker(name = "itemBreaker", fallbackMethod = "fallbackGetItem")
    public ItemDTO systemProtectedCall(Long id) {
        log.info("Attempting to fetch item with id {} in systemProtectedCall", id);
        return itemRetryService.fetchItem(id);
    }

    public ItemDTO fallbackGetItem(Long itemId, Throwable t) {

        log.error("Fallback triggered for itemId {} due to {}", itemId, t.toString());
        if (t instanceof ItemNotFoundException) {
            throw new ItemNotFoundException("Item " + itemId + " not found");
        }

        if (t instanceof feign.FeignException.NotFound) {
            throw new ItemNotFoundException("Item " + itemId + " not found");
        }

        ItemDTO dto = new ItemDTO();
        dto.setId(itemId);
        dto.setName("Item Service unavailable");
        dto.setPrice(0.0);
        dto.setQuantity(0);

        return dto;
    }
}