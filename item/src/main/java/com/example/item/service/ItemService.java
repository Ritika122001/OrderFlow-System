package com.example.item.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.item.dto.ItemDTO;
import com.example.item.entities.Item;
import com.example.item.exceptions.ItemNotFoundException;
import com.example.item.repository.ItemRepository;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemCacheService itemCacheService;

    public ItemService(ItemRepository itemRepository, ItemCacheService itemCacheService) {
        this.itemRepository = itemRepository;
        this.itemCacheService = itemCacheService;
    }

    public ItemDTO createItem(ItemDTO dto) {

        // Check if ID is provided
        Optional<Item> itemPresent = itemRepository.findByName(dto.getName());
        if (itemPresent.isPresent()) {
            throw new RuntimeException("Item already exists with Name " + dto.getId());
        }

        // Create new item
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

    @Cacheable(value = "itemsCache", key = "#id")
    public ItemDTO getItemById(Long id) {

        ItemDTO item = itemCacheService.getCachedItem(id);
        if (item != null) {
            return item;
        }

        return itemRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ItemNotFoundException("Item not found: " + id));
    }

}