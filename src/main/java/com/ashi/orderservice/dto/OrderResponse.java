package com.ashi.orderservice.dto;

import com.ashi.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerName,
        String address,
        String paymentType,
        List<ProductResponse> products,
        BigDecimal totalAmount,
        OrderStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

