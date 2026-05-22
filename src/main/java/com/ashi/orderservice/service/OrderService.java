package com.ashi.orderservice.service;

import com.ashi.orderservice.dto.OrderRequest;
import com.ashi.orderservice.dto.OrderResponse;
import com.ashi.orderservice.dto.UpdateOrderRequest;
import com.ashi.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse updateOrder(UUID id, UpdateOrderRequest request);

    OrderStatus trackOrderStatus(UUID id);

    void deleteOrder(UUID id);

    List<OrderResponse> getAllOrders();

    Page<OrderResponse> searchOrders(String query, OrderStatus status, Pageable pageable);
}

