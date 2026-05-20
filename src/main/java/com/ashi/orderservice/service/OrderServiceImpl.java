package com.ashi.orderservice.service;

import com.ashi.orderservice.dto.OrderRequest;
import com.ashi.orderservice.dto.OrderResponse;
import com.ashi.orderservice.dto.UpdateOrderRequest;
import com.ashi.orderservice.entity.Order;
import com.ashi.orderservice.entity.OrderStatus;
import com.ashi.orderservice.exception.ResourceNotFoundException;
import com.ashi.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setProductName(request.getProductName());
        order.setQuantity(request.getQuantity());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus(OrderStatus.CREATED);

        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse updateOrder(UUID id, UpdateOrderRequest request) {
        Order order = findOrder(id);

        if (request.getCustomerName() != null) {
            order.setCustomerName(request.getCustomerName());
        }
        if (request.getProductName() != null) {
            order.setProductName(request.getProductName());
        }
        if (request.getQuantity() != null) {
            order.setQuantity(request.getQuantity());
        }
        if (request.getTotalAmount() != null) {
            order.setTotalAmount(request.getTotalAmount());
        }
        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }

        return toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatus trackOrderStatus(UUID id) {
        return findOrder(id).getStatus();
    }

    @Override
    public void deleteOrder(UUID id) {
        Order order = findOrder(id);
        orderRepository.delete(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> searchOrders(String query, OrderStatus status) {
        String cleanedQuery = (query == null || query.isBlank()) ? null : query.trim();
        return orderRepository.search(cleanedQuery, status).stream()
                .map(this::toResponse)
                .toList();
    }

    private Order findOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.orderNotFound(id));
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getProductName(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}

