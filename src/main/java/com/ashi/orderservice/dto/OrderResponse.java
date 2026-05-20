package com.ashi.orderservice.dto;

import com.ashi.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerName,
        String productName,
        Integer quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

