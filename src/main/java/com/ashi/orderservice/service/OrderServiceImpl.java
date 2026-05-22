package com.ashi.orderservice.service;

import com.ashi.orderservice.dto.OrderRequest;
import com.ashi.orderservice.dto.OrderResponse;
import com.ashi.orderservice.dto.ProductRequest;
import com.ashi.orderservice.dto.ProductResponse;
import com.ashi.orderservice.dto.UpdateOrderRequest;
import com.ashi.orderservice.entity.Order;
import com.ashi.orderservice.entity.OrderProduct;
import com.ashi.orderservice.entity.OrderStatus;
import com.ashi.orderservice.exception.InvalidOrderUpdateException;
import com.ashi.orderservice.exception.ResourceNotFoundException;
import com.ashi.orderservice.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        order.setAddress(request.getAddress());
        order.setPaymentType(request.getPaymentType());
        order.setProducts(toOrderProducts(request.getProducts()));
        order.setTotalAmount(calculateTotalAmount(order.getProducts()));
        order.setStatus(OrderStatus.CREATED);

        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse updateOrder(UUID id, UpdateOrderRequest request) {
        Order order = findOrder(id);

        validateOrderIsUpdatable(order);

        if (request.getStatus() != null) {
            validateRequestedStatusForUpdate(request.getStatus());
        }

        if (request.getCustomerName() != null) {
            order.setCustomerName(request.getCustomerName());
        }
        if (request.getAddress() != null) {
            order.setAddress(request.getAddress());
        }
        if (request.getPaymentType() != null) {
            order.setPaymentType(request.getPaymentType());
        }
        if (request.getProducts() != null) {
            order.setProducts(toOrderProducts(request.getProducts()));
            order.setTotalAmount(calculateTotalAmount(order.getProducts()));
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
    public Page<OrderResponse> searchOrders(String query, OrderStatus status, Pageable pageable) {
        String cleanedQuery = (query == null || query.isBlank()) ? null : query.trim();
        if (cleanedQuery == null && status == null) {
            return orderRepository.findAll(pageable).map(this::toResponse);
        }
        return orderRepository.search(cleanedQuery, status, pageable).map(this::toResponse);
    }

    private void validateOrderIsUpdatable(Order order) {
        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PROCESSING) {
            throw new InvalidOrderUpdateException("Order can be updated only when status is CREATED or PROCESSING");
        }
    }

    private void validateRequestedStatusForUpdate(OrderStatus status) {
        if (status != OrderStatus.CREATED && status != OrderStatus.PROCESSING) {
            throw new InvalidOrderUpdateException("Only CREATED or PROCESSING status is allowed in update request");
        }
    }

    private Order findOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.orderNotFound(id));
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getAddress(),
                order.getPaymentType(),
                order.getProducts().stream()
                        .map(product -> new ProductResponse(
                                product.getProductId(),
                                product.getProductName(),
                                product.getPrice()
                        ))
                        .toList(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private List<OrderProduct> toOrderProducts(List<ProductRequest> products) {
        return products.stream()
                .map(product -> {
                    OrderProduct orderProduct = new OrderProduct();
                    orderProduct.setProductId(product.getProductId());
                    orderProduct.setProductName(product.getProductName());
                    orderProduct.setPrice(product.getPrice());
                    return orderProduct;
                })
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private BigDecimal calculateTotalAmount(List<OrderProduct> products) {
        return products.stream()
                .map(OrderProduct::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

