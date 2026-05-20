package com.ashi.orderservice.controller;

import com.ashi.orderservice.dto.OrderResponse;
import com.ashi.orderservice.entity.OrderStatus;
import com.ashi.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void createOrderEndpointShouldReturnCreated() throws Exception {
        OrderResponse response = sampleResponse();
        when(orderService.createOrder(any())).thenReturn(response);

        String body = """
                {
                  "customerName": "Ashish",
                  "productName": "Keyboard",
                  "quantity": 1,
                  "totalAmount": 59.99
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Ashish"));
    }

    @Test
    void updateOrderEndpointShouldReturnUpdatedOrder() throws Exception {
        UUID id = UUID.randomUUID();
        OrderResponse response = sampleResponse();
        when(orderService.updateOrder(eq(id), any())).thenReturn(response);

        String body = """
                { "status": "SHIPPED" }
                """;

        mockMvc.perform(put("/api/orders/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void trackOrderStatusEndpointShouldReturnStatus() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.trackOrderStatus(id)).thenReturn(OrderStatus.DELIVERED);

        mockMvc.perform(get("/api/orders/{id}/status", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void deleteOrderEndpointShouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(orderService).deleteOrder(id);

        mockMvc.perform(delete("/api/orders/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllOrdersEndpointShouldReturnList() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Keyboard"));
    }

    @Test
    void searchOrdersEndpointShouldReturnMatchingOrders() throws Exception {
        when(orderService.searchOrders("Ashish", OrderStatus.CREATED)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/orders/search")
                        .queryParam("query", "Ashish")
                        .queryParam("status", "CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerName").value("Ashish"));
    }

    private OrderResponse sampleResponse() {
        return new OrderResponse(
                UUID.randomUUID(),
                "Ashish",
                "Keyboard",
                1,
                new BigDecimal("59.99"),
                OrderStatus.CREATED,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }
}

