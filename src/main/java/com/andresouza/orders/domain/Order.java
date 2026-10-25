package com.andresouza.orders.domain;

import com.andresouza.orders.exception.BusinessException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private AppUser customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Order() {
    }

    public Order(AppUser customer) {
        this.customer = customer;
    }

    public void addItem(Product product, int quantity) {
        product.removeStock(quantity);
        OrderItem item = new OrderItem(this, product, quantity);
        items.add(item);
        total = total.add(item.getSubtotal());
    }

    public void changeStatus(OrderStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new BusinessException("Transição de status inválida: %s -> %s".formatted(status, target));
        }
        if (target == OrderStatus.CANCELLED) {
            items.forEach(item -> item.getProduct().restoreStock(item.getQuantity()));
        }
        status = target;
        updatedAt = Instant.now();
    }

    public boolean belongsTo(Long userId) {
        return customer.getId().equals(userId);
    }

    public Long getId() { return id; }
    public AppUser getCustomer() { return customer; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
