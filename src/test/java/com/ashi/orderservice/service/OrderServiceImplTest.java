package com.ashi.orderservice.service;

import com.ashi.orderservice.dto.OrderRequest;
import com.ashi.orderservice.dto.OrderResponse;
import com.ashi.orderservice.dto.ProductRequest;
import com.ashi.orderservice.dto.UpdateOrderRequest;
import com.ashi.orderservice.entity.Order;
import com.ashi.orderservice.entity.OrderProduct;
import com.ashi.orderservice.entity.OrderStatus;
import com.ashi.orderservice.exception.InvalidOrderUpdateException;
import com.ashi.orderservice.exception.ResourceNotFoundException;
import com.ashi.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
        request.setAddress("Bangalore, India");
        request.setPaymentType("UPI");
        request.setProducts(List.of(
                productRequest("Keyboard", "59.99"),
                productRequest("Mouse", "19.99")
        ));

        Order saved = sampleOrder();
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.customerName()).isEqualTo("Ashish");
        assertThat(response.address()).isEqualTo("Bangalore, India");
        assertThat(response.paymentType()).isEqualTo("UPI");
        assertThat(response.products()).hasSize(2);
        assertThat(response.totalAmount()).isEqualByComparingTo("79.98");
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateOrderShouldUpdateProvidedFields() {
        UUID id = UUID.randomUUID();
        Order existing = sampleOrder();
        existing.setId(id);

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatus(OrderStatus.PROCESSING);
        request.setAddress("Pune, India");
        request.setPaymentType("CARD");
        request.setProducts(List.of(productRequest("Monitor", "199.99")));

        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(orderRepository.save(existing)).thenReturn(existing);

        OrderResponse response = orderService.updateOrder(id, request);

        assertThat(response.status()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(response.address()).isEqualTo("Pune, India");
        assertThat(response.paymentType()).isEqualTo("CARD");
        assertThat(response.products()).hasSize(1);
        assertThat(response.products().get(0).productName()).isEqualTo("Monitor");
        assertThat(response.totalAmount()).isEqualByComparingTo("199.99");
    }

    @Test
    void updateOrderShouldSupportRemovingAndAddingProducts() {
        UUID id = UUID.randomUUID();
        Order existing = sampleOrder();
        existing.setId(id);

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setProducts(List.of(
                productRequest("Keyboard", "59.99"),
                productRequest("Headset", "39.99")
        ));

        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(orderRepository.save(existing)).thenReturn(existing);

        OrderResponse response = orderService.updateOrder(id, request);

        assertThat(response.products()).hasSize(2);
        assertThat(response.products().stream().map(p -> p.productName()).toList())
                .containsExactly("Keyboard", "Headset");
        assertThat(response.totalAmount()).isEqualByComparingTo("99.98");
    }

    @Test
    void updateOrderShouldRejectWhenCurrentOrderStatusIsNotEditable() {
        UUID id = UUID.randomUUID();
        Order existing = sampleOrder();
        existing.setId(id);
        existing.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> orderService.updateOrder(id, new UpdateOrderRequest()))
                .isInstanceOf(InvalidOrderUpdateException.class)
                .hasMessageContaining("CREATED or PROCESSING");
    }

    @Test
    void updateOrderShouldRejectWhenRequestedStatusIsNotEditable() {
        UUID id = UUID.randomUUID();
        Order existing = sampleOrder();
        existing.setId(id);

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> orderService.updateOrder(id, request))
                .isInstanceOf(InvalidOrderUpdateException.class)
                .hasMessageContaining("Only CREATED or PROCESSING");
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
        assertThat(orders.get(0).products().get(0).productName()).isEqualTo("Keyboard");
    }

    @Test
    void searchOrdersShouldPassNormalizedQueryToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.search("Ashish", OrderStatus.CREATED, pageable))
                .thenReturn(new PageImpl<>(List.of(sampleOrder()), pageable, 1));

        Page<OrderResponse> orders = orderService.searchOrders("  Ashish ", OrderStatus.CREATED, pageable);

        assertThat(orders.getContent()).hasSize(1);
        verify(orderRepository).search("Ashish", OrderStatus.CREATED, pageable);
    }

    @Test
    void searchOrdersShouldSendNullQueryWhenBlankAndKeepStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.search(null, OrderStatus.PROCESSING, pageable))
                .thenReturn(new PageImpl<>(List.of(sampleOrder()), pageable, 1));

        Page<OrderResponse> orders = orderService.searchOrders("   ", OrderStatus.PROCESSING, pageable);

        assertThat(orders.getContent()).hasSize(1);
        verify(orderRepository).search(null, OrderStatus.PROCESSING, pageable);
    }

    @Test
    void searchOrdersShouldReturnAllPagedWhenNoFiltersPassed() {
        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(sampleOrder()), pageable, 1));

        Page<OrderResponse> orders = orderService.searchOrders(null, null, pageable);

        assertThat(orders.getContent()).hasSize(1);
        verify(orderRepository).findAll(pageable);
    }

    private Order sampleOrder() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomerName("Ashish");
        order.setAddress("Bangalore, India");
        order.setPaymentType("UPI");
        OrderProduct firstProduct = new OrderProduct();
        firstProduct.setProductId(UUID.randomUUID());
        firstProduct.setProductName("Keyboard");
        firstProduct.setPrice(new BigDecimal("59.99"));

        OrderProduct secondProduct = new OrderProduct();
        secondProduct.setProductId(UUID.randomUUID());
        secondProduct.setProductName("Mouse");
        secondProduct.setPrice(new BigDecimal("19.99"));

        order.setProducts(List.of(firstProduct, secondProduct));
        order.setTotalAmount(new BigDecimal("79.98"));
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(OffsetDateTime.now());
        order.setUpdatedAt(OffsetDateTime.now());
        return order;
    }

    private ProductRequest productRequest(String name, String price) {
        ProductRequest product = new ProductRequest();
        product.setProductId(UUID.randomUUID());
        product.setProductName(name);
        product.setPrice(new BigDecimal(price));
        return product;
    }
}

