package com.example.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.order.client.ItemClient;
import com.example.order.dto.ItemDTO;
import com.example.order.exceptions.ItemNotFoundException;

import feign.FeignException;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class ItemRetryService {

    private final ItemClient itemClient;

    private static final Logger log = LoggerFactory.getLogger(ItemRetryService.class);

    public ItemRetryService(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @Retry(name = "itemRetry")
    public ItemDTO fetchItem(Long id) {

        try {
            log.info("Fetching item {} in Retry from ItemClient", id);
            return itemClient.getItemById(id);

        } catch (FeignException.NotFound ex) {
            log.warn("Item {} not found (404 from Item Service)", id);
            throw new ItemNotFoundException("Item not found: " + id);

        } catch (FeignException ex) {
            log.error("Feign error while fetching item {}: {}", id, ex.status());
            throw ex;
        }
    }
}
