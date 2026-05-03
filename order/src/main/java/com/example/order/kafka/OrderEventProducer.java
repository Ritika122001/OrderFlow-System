package com.example.order.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.order.dto.ItemDTO;

@Component
public class OrderEventProducer {

    private final KafkaTemplate<Long, ItemDTO> kafkaTemplate;

    OrderEventProducer(KafkaTemplate<Long, ItemDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishEvent(String topic, Long key, ItemDTO payload) {
        kafkaTemplate.send(topic, key, payload);
    }

}