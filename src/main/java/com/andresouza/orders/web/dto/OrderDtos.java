package com.andresouza.orders.web.dto;

import com.andresouza.orders.domain.Order;
import com.andresouza.orders.domain.OrderItem;
import com.andresouza.orders.domain.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record OrderItemRequest(@NotNull Long productId, @NotNull @Positive @Max(1000) Integer quantity) {
    }

    public record CreateOrderRequest(@NotEmpty @Valid List<OrderItemRequest> items) {
    }

    public record UpdateStatusRequest(@NotNull OrderStatus status) {
    }

    public record OrderItemResponse(Long productId, String productName, int quantity,
                                    BigDecimal unitPrice, BigDecimal subtotal) {

        static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(item.getProduct().getId(), item.getProduct().getName(),
                    item.getQuantity(), item.getUnitPrice(), item.getSubtotal());
        }
    }

    public record OrderResponse(Long id, Long customerId, OrderStatus status, BigDecimal total,
                                List<OrderItemResponse> items, Instant createdAt, Instant updatedAt) {

        public static OrderResponse from(Order order) {
            return new OrderResponse(order.getId(), order.getCustomer().getId(), order.getStatus(), order.getTotal(),
                    order.getItems().stream().map(OrderItemResponse::from).toList(),
                    order.getCreatedAt(), order.getUpdatedAt());
        }
    }

    public record OrderSummaryResponse(Long id, Long customerId, String customerName, OrderStatus status,
                                       BigDecimal total, Instant createdAt) {

        public static OrderSummaryResponse from(Order order) {
            return new OrderSummaryResponse(order.getId(), order.getCustomer().getId(), order.getCustomer().getName(),
                    order.getStatus(), order.getTotal(), order.getCreatedAt());
        }
    }
}
