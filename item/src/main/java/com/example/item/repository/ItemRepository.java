package com.example.item.repository;

import com.example.item.entities.Item;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByName(String name);
}