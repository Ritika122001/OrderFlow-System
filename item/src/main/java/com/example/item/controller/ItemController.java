package com.example.item.controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.item.dto.ItemDTO;
import com.example.item.exceptions.ItemNotFoundException;
import com.example.item.service.ItemService;

import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService ItemService;

    public ItemController(ItemService ItemService) {
        this.ItemService = ItemService;
    }

    @PostMapping("/createItem")
    public ItemDTO createItem(@RequestBody ItemDTO dto) {
        return ItemService.createItem(dto);
    }

    @GetMapping("/getItems")
    public List<ItemDTO> getAllItems() {
        return ItemService.getAllItems();
    }

    private AtomicInteger externalHitCounter = new AtomicInteger(0);

    @GetMapping("/{id}")
    public ItemDTO getItemById(@PathVariable Long id) {
        // Fail first 3 hits, succeed on 4th and 5th
        int hit = externalHitCounter.incrementAndGet();
        System.out.println("External Hit #" + hit);

        // Fail for hits 2,3,5, etc. to test CB opening
        if (hit == 2 || hit == 3 || hit == 5) {
            System.out.println("Simulated failure for hit " + hit);
            throw new RuntimeException("Simulated failure");
        }

        ItemDTO item = ItemService.getItemById(id);
        if (item == null) {
            throw new ItemNotFoundException("Item not found: " + id);
        }

        return item;
    }

}