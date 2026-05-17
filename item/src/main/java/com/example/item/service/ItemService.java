package com.example.item.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.example.item.dto.ItemDTO;
import com.example.item.entities.Item;
import com.example.item.exceptions.ItemNotFoundException;
import com.example.item.repository.ItemRepository;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public ItemDTO createItem(ItemDTO dto) {

        Optional<Item> itemPresent = itemRepository.findByName(dto.getName());
        if (itemPresent.isPresent()) {
            throw new RuntimeException("Item already exists with Name " + dto.getId());
        }

        Item newItem = new Item();
        newItem.setName(dto.getName());
        newItem.setPrice(dto.getPrice());
        newItem.setQuantity(dto.getQuantity());

        Item saved = itemRepository.save(newItem);
        return convertToDTO(saved);
    }

    public List<ItemDTO> getAllItems() {
        return itemRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ItemDTO convertToDTO(Item Item) {
        ItemDTO dto = new ItemDTO();
        dto.setId(Item.getId());
        dto.setName(Item.getName());
        dto.setPrice(Item.getPrice());
        dto.setQuantity(Item.getQuantity());
        return dto;
    }

    private AtomicInteger externalHitCounter = new AtomicInteger(0);

    @Cacheable(value = "itemsCache", key = "#id")
    public ItemDTO getItemById(Long id) {

        int hit = externalHitCounter.incrementAndGet();
        log.info("External Hit # {}", hit);

        if (hit <= 3) {
            log.error("Simulated failure for hit {}", hit);
            throw new RuntimeException("Simulated failure");
        }

        return itemRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ItemNotFoundException("Item not found: " + id));
    }

}