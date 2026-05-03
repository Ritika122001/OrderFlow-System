package com.example.order.service;

import org.springframework.stereotype.Service;
import com.example.order.dto.ItemDTO;
import com.example.order.exceptions.ItemNotFoundException;
import com.example.order.kafka.OrderEventProducer;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class ItemServiceFallback {

    private final ItemRetryService itemRetryService;
    private final OrderEventProducer orderEventProducer;

    public ItemServiceFallback(ItemRetryService itemRetryService,
            OrderEventProducer orderEventProducer) {
        this.itemRetryService = itemRetryService;
        this.orderEventProducer = orderEventProducer;
    }

    @CircuitBreaker(name = "itemBreaker", fallbackMethod = "fallbackGetItem")
    public ItemDTO getItemWithCircuitBreaker(Long itemId) {
        return itemRetryService.fetchItem(itemId);
    }

    // fallback only triggered for system failures
    public ItemDTO fallbackGetItem(Long itemId, Throwable t) {
        ItemDTO fallback = new ItemDTO();
        fallback.setId(itemId);
        fallback.setName("Item Service unavailable");
        fallback.setQuantity(0);
        orderEventProducer.publishEvent("order-events", fallback.getId(), fallback);
        return fallback;
    }

}