package com.example.order.service;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.example.order.dto.ItemDTO;
import com.example.order.dto.ResponseOrderDTO;
import com.example.order.dto.OrderItemDTO;
import com.example.order.dto.RequestOrderDTO;
import com.example.order.entities.Order;
import com.example.order.entities.OrderItem;
import com.example.order.exceptions.ItemNotFoundException;
import com.example.order.repository.OrderRepository;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        // 1️⃣ Fetch the existing order
        Order order = orderRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Order not found for email: " + email));

        List<OrderItem> orderItems = order.getItems(); // managed collection

        // 2️⃣ Process each incoming item
        for (OrderItemDTO dto : orderDTO.getItems()) {

            // Call item service to validate item
            ItemDTO item = itemServiceFallback.getItemWithCircuitBreaker(dto.getItemId());

            // If fallback triggered, reject update for this item
            if (item == null || "Item Service unavailable".equals(item.getName())) {
                throw new RuntimeException("Item " + dto.getItemId() + " not available. Cannot update order.");
            }

            boolean found = false;

            // 3️⃣ Check if item already exists in order
            for (OrderItem existingItem : orderItems) {
                if (existingItem.getItemId().equals(dto.getItemId())) {
                    // Increment quantity
                    if (dto.getQuantity() > item.getQuantity()) {
                        throw new RuntimeException("Quantity is more than present!!");
                    } else {
                        existingItem.setQuantity(existingItem.getQuantity() + dto.getQuantity());
                        found = true;
                        break;
                    }
                }
            }

            // 4️⃣ If item does not exist, add as new
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

        // 5️⃣ Update order metadata
        order.setUpdatedDate(LocalDateTime.now());
        order.setOrderSignature(generateOrderSignatureFromEntities(order.getItems()));
        // 6️⃣ Save order (cascades to items)
        orderRepository.save(order);

        // 7️⃣ Return response with full item details
        return convertToDTOWithItemDetails(order);
    }

    private String generateOrderSignatureFromEntities(List<OrderItem> items) {
        return items.stream()
                .sorted((a, b) -> a.getItemId().compareTo(b.getItemId()))
                .map(item -> item.getItemId() + ":" + item.getQuantity())
                .collect(Collectors.joining(","));
    }

    private ResponseOrderDTO convertToDTOWithItemDetails(Order order) {

        List<OrderItem> list = order.getItems() != null ? order.getItems() : new ArrayList<>();

        List<ItemDTO> fullItemList = new ArrayList<>();
        for (OrderItem orderItem : list) {
            ItemDTO item = itemServiceFallback.getItemWithCircuitBreaker(orderItem.getItemId());
            if (item != null) {
                item.setQuantity(orderItem.getQuantity());
                fullItemList.add(item);
            }
        }

        return new ResponseOrderDTO(order.getId(), order.getEmail(), fullItemList);
    }

    @Transactional
    public ResponseOrderDTO createNewOrder(RequestOrderDTO orderDTO) {

        Optional<Order> existingOrderOpt = orderRepository.findByEmail(orderDTO.getEmail());
        if (existingOrderOpt.isPresent()) {
            throw new RuntimeException("Order for this customer already exists");
        }

        Order order = new Order();

        List<OrderItem> validItemsForDB = new ArrayList<>(); // will be saved
        List<ItemDTO> responseItems = new ArrayList<>(); // will go in response

        for (OrderItemDTO dto : orderDTO.getItems()) {
            try {
                ItemDTO item = itemServiceFallback.getItemWithCircuitBreaker(dto.getItemId());

                if (item == null || item.getId() == null) {
                    throw new ItemNotFoundException("Item not found!!"); // propagate actual exception
                }

                // Only add real items to DB
                OrderItem orderItem = new OrderItem(dto.getItemId(), dto.getQuantity());
                orderItem.setOrder(order);
                validItemsForDB.add(orderItem);

                // Add to response
                item.setQuantity(dto.getQuantity());
                responseItems.add(item);

            } catch (ItemNotFoundException ex) {
                // Decide here if you want to include fallback or just skip
                log.warn("Item {} not found: skipping", dto.getItemId());
                // Optionally add fallback item to response
            }
        }
        // Set valid items for DB
        // Only persist if there are real items to save
        if (!validItemsForDB.isEmpty()) {
            order.setItems(validItemsForDB);
            order.setEmail(orderDTO.getEmail());
            order.setOrderSignature(generateOrderSignatureFromEntities(validItemsForDB));
            order.setOrderDate(LocalDateTime.now());
            order.setUpdatedDate(LocalDateTime.now());
            orderRepository.save(order);
        } else {
            log.warn("No valid items to save for email {}, order will not be persisted", orderDTO.getEmail());
        }

        // Build response with all items (including fallback)
        ResponseOrderDTO responseOrderDTO = new ResponseOrderDTO(order.getId(), order.getEmail(), responseItems);
        return responseOrderDTO;
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