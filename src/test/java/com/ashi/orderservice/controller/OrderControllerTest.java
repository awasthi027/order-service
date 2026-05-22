package com.ashi.orderservice.controller;

import com.ashi.orderservice.dto.OrderResponse;
import com.ashi.orderservice.dto.ProductResponse;
import com.ashi.orderservice.entity.OrderStatus;
import com.ashi.orderservice.exception.InvalidOrderUpdateException;
import com.ashi.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
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
                  "address": "Bangalore, India",
                  "paymentType": "UPI",
                  "products": [
                    {
                      "productId": "d3e4f262-6af5-4e8c-932f-b6c604f76295",
                      "productName": "Keyboard",
                      "price": 59.99
                    },
                    {
                      "productId": "40ad8af8-2e8e-48e4-9d8e-2946f333f640",
                      "productName": "Mouse",
                      "price": 19.99
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Ashish"))
                .andExpect(jsonPath("$.address").value("Bangalore, India"))
                .andExpect(jsonPath("$.products[0].productName").value("Keyboard"));
    }

    @Test
    void updateOrderEndpointShouldReturnUpdatedOrder() throws Exception {
        UUID id = UUID.randomUUID();
        OrderResponse response = sampleResponse();
        when(orderService.updateOrder(eq(id), any())).thenReturn(response);

        String body = """
                { "status": "PROCESSING" }
                """;

        mockMvc.perform(put("/api/orders/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void updateOrderShouldReturnBadRequestForDisallowedStatus() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.updateOrder(eq(id), any()))
                .thenThrow(new InvalidOrderUpdateException("Only CREATED or PROCESSING status is allowed in update request"));

        String body = """
                { "status": "SHIPPED" }
                """;

        mockMvc.perform(put("/api/orders/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only CREATED or PROCESSING status is allowed in update request"));
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
                .andExpect(jsonPath("$[0].products[0].productName").value("Keyboard"));
    }

    @Test
    void searchOrdersEndpointShouldReturnMatchingOrders() throws Exception {
        when(orderService.searchOrders(eq("Ashish"), eq(OrderStatus.CREATED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/orders/search")
                        .queryParam("query", "Ashish")
                        .queryParam("status", "CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerName").value("Ashish"));
    }

    @Test
    void searchOrdersShouldTreatEmptyQueryAsNull() throws Exception {
        when(orderService.searchOrders(nullable(String.class), nullable(OrderStatus.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/orders/search")
                        .queryParam("query", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerName").value("Ashish"));

        verify(orderService).searchOrders(isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void searchOrdersShouldMapInprogessStatusToProcessing() throws Exception {
        when(orderService.searchOrders(isNull(), eq(OrderStatus.PROCESSING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/orders/search")
                        .queryParam("status", "inprogess"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerName").value("Ashish"));

        verify(orderService).searchOrders(isNull(), eq(OrderStatus.PROCESSING), any(Pageable.class));
    }

    @Test
    void searchOrdersShouldReturnPagedAllOrdersWhenFiltersNotPassed() throws Exception {
        when(orderService.searchOrders(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/orders/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerName").value("Ashish"));
    }

    @Test
    void searchOrdersShouldReturnBadRequestForInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/orders/search")
                        .queryParam("status", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid status. Allowed values: CREATED, PROCESSING, SHIPPED, DELIVERED, CANCELLED"));
    }

    private OrderResponse sampleResponse() {
        return new OrderResponse(
                UUID.randomUUID(),
                "Ashish",
                "Bangalore, India",
                "UPI",
                List.of(
                        new ProductResponse(UUID.randomUUID(), "Keyboard", new BigDecimal("59.99")),
                        new ProductResponse(UUID.randomUUID(), "Mouse", new BigDecimal("19.99"))
                ),
                new BigDecimal("79.98"),
                OrderStatus.CREATED,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }
}

