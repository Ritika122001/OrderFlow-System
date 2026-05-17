package com.example.order.service;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.example.order.dto.ItemDTO;
import com.example.order.dto.ResponseOrderDTO;
import com.example.order.dto.OrderItemDTO;
import com.example.order.dto.RequestOrderDTO;
import com.example.order.entities.Order;
import com.example.order.entities.OrderItem;
import com.example.order.repository.OrderRepository;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemServiceFallback itemServiceFallback;

    private Logger log = LoggerFactory.getLogger(OrderService.class);

    public OrderService(OrderRepository orderRepository, ItemServiceFallback itemServiceFallback) {
        this.orderRepository = orderRepository;
        this.itemServiceFallback = itemServiceFallback;
    }

    @Transactional
    public ResponseOrderDTO updateOrder(String email, RequestOrderDTO orderDTO) {

        Order order = orderRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Order not found for email: " + email));

        List<OrderItem> orderItems = order.getItems();

        for (OrderItemDTO dto : orderDTO.getItems()) {

            ItemDTO item = itemServiceFallback.systemProtectedCall(dto.getItemId());

            if (item == null || "Item Service unavailable".equals(item.getName())) {
                throw new RuntimeException("Item " + dto.getItemId() + " not available. Cannot update order.");
            }

            boolean found = false;

            for (OrderItem existingItem : orderItems) {
                if (existingItem.getItemId().equals(dto.getItemId())) {
                    if (dto.getQuantity() > item.getQuantity()) {
                        throw new RuntimeException("Quantity is more than present!!");
                    } else {
                        existingItem.setQuantity(existingItem.getQuantity() + dto.getQuantity());
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                OrderItem newItem = new OrderItem(dto.getItemId(), dto.getQuantity());
                if (dto.getQuantity() > item.getQuantity()) {
                    throw new RuntimeException("Quantity is more than present!!");
                } else {
                    newItem.setOrder(order);
                    orderItems.add(newItem);
                }
            }
        }

        order.setUpdatedDate(LocalDateTime.now());
        orderRepository.save(order);

        return convertToDTOWithItemDetails(order);
    }

    private ResponseOrderDTO convertToDTOWithItemDetails(Order order) {

        List<OrderItem> list = order.getItems() != null ? order.getItems() : new ArrayList<>();

        List<ItemDTO> fullItemList = new ArrayList<>();
        for (OrderItem orderItem : list) {
            ItemDTO item = itemServiceFallback.systemProtectedCall(orderItem.getItemId());
            if (item != null) {
                item.setQuantity(orderItem.getQuantity());
                fullItemList.add(item);
            }
        }

        return new ResponseOrderDTO(order.getId(), order.getEmail(), fullItemList);
    }

    @Transactional
    public ResponseOrderDTO createNewOrder(RequestOrderDTO orderDTO) {

        Order order = new Order();
        order.setEmail(orderDTO.getEmail());
        order.setOrderDate(LocalDateTime.now());
        order.setUpdatedDate(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();
        List<ItemDTO> responseItems = new ArrayList<>();

        log.info("Creating order for email: {}", orderDTO.getEmail());

        for (OrderItemDTO dto : orderDTO.getItems()) {

            log.info("Processing itemId={} qty={}", dto.getItemId(), dto.getQuantity());

            ItemDTO item = itemServiceFallback.systemProtectedCall(dto.getItemId());

            OrderItem orderItem = new OrderItem(dto.getItemId(), dto.getQuantity());
            orderItem.setOrder(order);

            orderItems.add(orderItem);
            responseItems.add(item);
        }

        if (orderItems.isEmpty()) {
            throw new IllegalStateException("No valid items to create order");
        }

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        return new ResponseOrderDTO(
                savedOrder.getId(),
                savedOrder.getEmail(),
                responseItems);
    }

    public List<ResponseOrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<ResponseOrderDTO> list = new ArrayList<>();
        for (Order ord : orders) {
            ResponseOrderDTO dto = convertToDTOWithItemDetails(ord);
            list.add(dto);
        }
        return list;
    }

    public ResponseOrderDTO getOrderById(Long id) {
        Optional<Order> order = orderRepository.findById(id);
        return convertToDTOWithItemDetails(order.get());
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

}