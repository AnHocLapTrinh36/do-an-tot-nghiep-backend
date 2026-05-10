package com.example.Backend.repository;

import com.example.Backend.entity.Order;
import com.example.Backend.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(Long orderCode);

    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    boolean existsByBuyerIdAndProjectIdAndStatus(Long buyerId, Long projectId, OrderStatus status);
}
