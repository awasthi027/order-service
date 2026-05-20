package com.ashi.orderservice.service;

import com.ashi.orderservice.dto.OrderRequest;
import com.ashi.orderservice.dto.OrderResponse;
import com.ashi.orderservice.dto.UpdateOrderRequest;
import com.ashi.orderservice.entity.Order;
import com.ashi.orderservice.entity.OrderStatus;
import com.ashi.orderservice.exception.ResourceNotFoundException;
import com.ashi.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrderShouldPersistAndReturnResponse() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Ashish");
        request.setProductName("Keyboard");
        request.setQuantity(1);
        request.setTotalAmount(new BigDecimal("59.99"));

        Order saved = sampleOrder();
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.customerName()).isEqualTo("Ashish");
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateOrderShouldUpdateProvidedFields() {
        UUID id = UUID.randomUUID();
        Order existing = sampleOrder();
        existing.setId(id);

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatus(OrderStatus.SHIPPED);
        request.setQuantity(2);

        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(orderRepository.save(existing)).thenReturn(existing);

        OrderResponse response = orderService.updateOrder(id, request);

        assertThat(response.status()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(response.quantity()).isEqualTo(2);
    }

    @Test
    void updateOrderShouldThrowWhenOrderMissing() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrder(id, new UpdateOrderRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void trackOrderStatusShouldReturnCurrentStatus() {
        UUID id = UUID.randomUUID();
        Order existing = sampleOrder();
        existing.setId(id);
        existing.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThat(orderService.trackOrderStatus(id)).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void deleteOrderShouldDeleteWhenExists() {
        UUID id = UUID.randomUUID();
        Order existing = sampleOrder();
        existing.setId(id);

        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));
        doNothing().when(orderRepository).delete(existing);

        orderService.deleteOrder(id);

        verify(orderRepository).delete(existing);
    }

    @Test
    void getAllOrdersShouldReturnMappedResponse() {
        when(orderRepository.findAll()).thenReturn(List.of(sampleOrder()));

        List<OrderResponse> orders = orderService.getAllOrders();

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).productName()).isEqualTo("Keyboard");
    }

    @Test
    void searchOrdersShouldPassNormalizedQueryToRepository() {
        when(orderRepository.search("Ashish", OrderStatus.CREATED)).thenReturn(List.of(sampleOrder()));

        List<OrderResponse> orders = orderService.searchOrders("  Ashish ", OrderStatus.CREATED);

        assertThat(orders).hasSize(1);
        verify(orderRepository).search("Ashish", OrderStatus.CREATED);
    }

    private Order sampleOrder() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomerName("Ashish");
        order.setProductName("Keyboard");
        order.setQuantity(1);
        order.setTotalAmount(new BigDecimal("59.99"));
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(OffsetDateTime.now());
        order.setUpdatedAt(OffsetDateTime.now());
        return order;
    }
}

