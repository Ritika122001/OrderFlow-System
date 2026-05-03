package com.example.order.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ResponseOrderDTO {
    private Long id;
    private String email;
    private List<ItemDTO> items;
}