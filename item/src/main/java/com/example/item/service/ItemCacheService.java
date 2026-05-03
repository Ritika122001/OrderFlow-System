package com.example.item.service;

import com.example.item.dto.ItemDTO;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.cache.Cache;

@Service
public class ItemCacheService {

    private final CacheManager cacheManager;

    ItemCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public ItemDTO getCachedItem(Long itemId) {
        System.out.println("CacheItem calling");
        Cache cache = cacheManager.getCache("itemsCache");
        return cache != null ? cache.get(itemId, ItemDTO.class) : null;
    }

}
