package com.fintrack.client.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class Category {
    private UUID id;
    private String name;
    private UUID userId;
}
