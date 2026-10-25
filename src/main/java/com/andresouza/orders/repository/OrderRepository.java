package com.andresouza.orders.repository;

import com.andresouza.orders.domain.Order;
import com.andresouza.orders.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Carrega itens e produtos em uma única consulta (evita N+1)
    @EntityGraph(attributePaths = {"customer", "items", "items.product"})
    Optional<Order> findWithItemsById(Long id);

    @EntityGraph(attributePaths = {"customer"})
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"customer"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"customer"})
    Page<Order> findAllBy(Pageable pageable);
}
