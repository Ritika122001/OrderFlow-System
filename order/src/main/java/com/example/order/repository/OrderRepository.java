package com.example.order.repository;

import com.example.order.entities.Order;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Optional<Order> findByEmailAndOrderSignature(String email, String signature);

    Optional<Order> findByEmail(String email);
}