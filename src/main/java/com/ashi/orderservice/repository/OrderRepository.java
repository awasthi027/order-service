package com.ashi.orderservice.repository;

import com.ashi.orderservice.entity.Order;
import com.ashi.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("""
            SELECT o FROM Order o
            WHERE (:query IS NULL OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(o.productName) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:status IS NULL OR o.status = :status)
            ORDER BY o.createdAt DESC
            """)
    List<Order> search(@Param("query") String query, @Param("status") OrderStatus status);
}

