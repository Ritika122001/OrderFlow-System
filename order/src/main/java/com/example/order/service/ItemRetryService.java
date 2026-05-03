package com.example.order.service;

import org.springframework.stereotype.Service;
import com.example.order.client.ItemClient;
import com.example.order.dto.ItemDTO;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class ItemRetryService {

    private ItemClient itemClient;

    ItemRetryService(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @Retry(name = "itemRetry")
    public ItemDTO fetchItem(Long id) {
        System.out.println("Calling item service in retry...");
        return itemClient.getItemById(id);

    }
}
