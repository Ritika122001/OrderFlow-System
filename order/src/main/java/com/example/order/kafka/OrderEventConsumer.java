package com.example.order.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.order.dto.ItemDTO;

@Component
public class OrderEventConsumer {

    @KafkaListener(topics = "order-events", groupId = "order-group")
    public void consume(ItemDTO itemDTO) {
        System.out.println("Received Item from Kafka: "
                + itemDTO.getName() + ", quantity: " + itemDTO.getQuantity());
    }

}
