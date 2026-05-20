package com.ashi.orderservice.controller;

import com.ashi.orderservice.dto.OrderRequest;
import com.ashi.orderservice.dto.OrderResponse;
import com.ashi.orderservice.dto.UpdateOrderRequest;
import com.ashi.orderservice.entity.OrderStatus;
import com.ashi.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @PutMapping("/{id}")
    public OrderResponse updateOrder(@PathVariable UUID id, @Valid @RequestBody UpdateOrderRequest request) {
        return orderService.updateOrder(id, request);
    }

    @GetMapping("/{id}/status")
    public Map<String, Object> trackOrderStatus(@PathVariable UUID id) {
        return Map.of(
                "orderId", id,
                "status", orderService.trackOrderStatus(id)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable UUID id) {
        orderService.deleteOrder(id);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/search")
    public List<OrderResponse> searchOrders(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) OrderStatus status
    ) {
        return orderService.searchOrders(query, status);
    }
}

