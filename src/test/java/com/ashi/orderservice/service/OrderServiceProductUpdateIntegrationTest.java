package com.ashi.orderservice.service;

import com.ashi.orderservice.dto.OrderRequest;
import com.ashi.orderservice.dto.OrderResponse;
import com.ashi.orderservice.dto.ProductRequest;
import com.ashi.orderservice.dto.UpdateOrderRequest;
import com.ashi.orderservice.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceProductUpdateIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Test
    void shouldPersistProductChangesWhenProductsAreAddedOrRemoved() {
        OrderRequest createRequest = new OrderRequest();
        createRequest.setCustomerName("Ashish");
        createRequest.setAddress("Bangalore");
        createRequest.setPaymentType("UPI");
        createRequest.setProducts(List.of(
                product("Keyboard", "59.99"),
                product("Mouse", "19.99")
        ));

        OrderResponse created = orderService.createOrder(createRequest);
        assertThat(created.products()).hasSize(2);
        assertThat(created.totalAmount()).isEqualByComparingTo("79.98");

        UpdateOrderRequest updateRequest = new UpdateOrderRequest();
        updateRequest.setProducts(List.of(
                product("Keyboard", "59.99"),
                product("Headset", "39.99")
        ));
        updateRequest.setStatus(OrderStatus.PROCESSING);

        OrderResponse updated = orderService.updateOrder(created.id(), updateRequest);
        assertThat(updated.products()).hasSize(2);
        assertThat(updated.products().stream().map(p -> p.productName()).toList())
                .containsExactly("Keyboard", "Headset");
        assertThat(updated.totalAmount()).isEqualByComparingTo("99.98");

        // Read again to ensure product changes were persisted, not only returned in-memory.
        OrderResponse reloaded = orderService.getAllOrders().stream()
                .filter(order -> order.id().equals(created.id()))
                .findFirst()
                .orElseThrow();

        assertThat(reloaded.products().stream().map(p -> p.productName()).toList())
                .containsExactly("Keyboard", "Headset");
        assertThat(reloaded.totalAmount()).isEqualByComparingTo("99.98");
    }

    private ProductRequest product(String name, String price) {
        ProductRequest product = new ProductRequest();
        product.setProductId(UUID.randomUUID());
        product.setProductName(name);
        product.setPrice(new BigDecimal(price));
        return product;
    }
}

