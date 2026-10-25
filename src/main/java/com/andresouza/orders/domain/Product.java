package com.andresouza.orders.domain;

import com.andresouza.orders.exception.BusinessException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(nullable = false)
    private boolean active = true;

    // Lock otimista: evita vender o mesmo estoque em dois pedidos simultâneos
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Product() {
    }

    public Product(String name, String description, BigDecimal price, int stockQuantity) {
        update(name, description, price, stockQuantity);
    }

    public void update(String name, String description, BigDecimal price, int stockQuantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    /** Baixa de estoque ao incluir o produto em um pedido. */
    public void removeStock(int quantity) {
        if (!active) {
            throw new BusinessException("Produto %d está inativo".formatted(id));
        }
        if (quantity > stockQuantity) {
            throw new BusinessException("Estoque insuficiente para o produto %d (disponível: %d, solicitado: %d)"
                    .formatted(id, stockQuantity, quantity));
        }
        stockQuantity -= quantity;
    }

    /** Devolve ao estoque os itens de um pedido cancelado. */
    public void restoreStock(int quantity) {
        stockQuantity += quantity;
    }

    public void deactivate() {
        this.active = false;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
