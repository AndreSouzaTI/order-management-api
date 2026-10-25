package com.andresouza.orders.domain;

import com.andresouza.orders.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private final AppUser customer = new AppUser("Maria", "maria@email.com", "hash", Role.USER);

    @Test
    void addItemCalculatesTotalAndRemovesStock() {
        Product keyboard = new Product("Teclado", null, new BigDecimal("100.00"), 10);
        Product mouse = new Product("Mouse", null, new BigDecimal("50.50"), 5);
        Order order = new Order(customer);

        order.addItem(keyboard, 2);
        order.addItem(mouse, 1);

        assertThat(order.getTotal()).isEqualByComparingTo("250.50");
        assertThat(order.getItems()).hasSize(2);
        assertThat(keyboard.getStockQuantity()).isEqualTo(8);
        assertThat(mouse.getStockQuantity()).isEqualTo(4);
    }

    @Test
    void addItemFailsWhenStockIsInsufficient() {
        Product monitor = new Product("Monitor", null, new BigDecimal("900.00"), 1);
        Order order = new Order(customer);

        assertThatThrownBy(() -> order.addItem(monitor, 2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estoque insuficiente");
        assertThat(monitor.getStockQuantity()).isEqualTo(1);
    }

    @Test
    void cancelRestoresStock() {
        Product headset = new Product("Headset", null, new BigDecimal("300.00"), 3);
        Order order = new Order(customer);
        order.addItem(headset, 3);

        order.changeStatus(OrderStatus.CANCELLED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(headset.getStockQuantity()).isEqualTo(3);
    }

    @Test
    void rejectsInvalidStatusTransition() {
        Order order = new Order(customer);
        order.changeStatus(OrderStatus.PAID);
        order.changeStatus(OrderStatus.SHIPPED);

        assertThatThrownBy(() -> order.changeStatus(OrderStatus.CANCELLED))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("SHIPPED -> CANCELLED");
    }
}
