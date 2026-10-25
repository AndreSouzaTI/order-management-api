package com.andresouza.orders.web.dto;

import com.andresouza.orders.domain.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class ProductDtos {

    private ProductDtos() {
    }

    public record ProductRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 500) String description,
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal price,
            @NotNull @PositiveOrZero Integer stockQuantity) {
    }

    public record ProductResponse(Long id, String name, String description, BigDecimal price, int stockQuantity) {

        public static ProductResponse from(Product product) {
            return new ProductResponse(product.getId(), product.getName(), product.getDescription(),
                    product.getPrice(), product.getStockQuantity());
        }
    }
}
