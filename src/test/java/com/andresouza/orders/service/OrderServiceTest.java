package com.andresouza.orders.service;

import com.andresouza.orders.domain.AppUser;
import com.andresouza.orders.domain.Order;
import com.andresouza.orders.domain.Product;
import com.andresouza.orders.domain.Role;
import com.andresouza.orders.exception.ResourceNotFoundException;
import com.andresouza.orders.repository.OrderRepository;
import com.andresouza.orders.repository.ProductRepository;
import com.andresouza.orders.repository.UserRepository;
import com.andresouza.orders.security.CurrentUser;
import com.andresouza.orders.web.dto.OrderDtos.CreateOrderRequest;
import com.andresouza.orders.web.dto.OrderDtos.OrderItemRequest;
import com.andresouza.orders.web.dto.OrderDtos.OrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orders;
    @Mock
    private ProductRepository products;
    @Mock
    private UserRepository users;
    @InjectMocks
    private OrderService service;

    private final CurrentUser maria = new CurrentUser(1L, false);

    @Test
    void createMergesRepeatedProductsIntoOneItem() {
        AppUser customer = user(1L);
        Product notebook = product(10L, "5000.00", 5);
        when(users.findById(1L)).thenReturn(Optional.of(customer));
        when(products.findAllById(anyIterable())).thenReturn(List.of(notebook));
        when(orders.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = service.create(maria, new CreateOrderRequest(List.of(
                new OrderItemRequest(10L, 1), new OrderItemRequest(10L, 2))));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().quantity()).isEqualTo(3);
        assertThat(response.total()).isEqualByComparingTo("15000.00");
        assertThat(notebook.getStockQuantity()).isEqualTo(2);
    }

    @Test
    void createFailsWhenProductDoesNotExist() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(products.findAllById(anyIterable())).thenReturn(List.of());

        assertThatThrownBy(() -> service.create(maria, new CreateOrderRequest(List.of(new OrderItemRequest(99L, 1)))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(orders, never()).save(any());
    }

    @Test
    void customerCannotSeeOrderFromAnotherCustomer() {
        Order order = new Order(user(2L));
        when(orders.findWithItemsById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.findById(maria, 5L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminCanSeeAnyOrder() {
        Order order = new Order(user(2L));
        when(orders.findWithItemsById(5L)).thenReturn(Optional.of(order));

        OrderResponse response = service.findById(new CurrentUser(99L, true), 5L);

        assertThat(response.customerId()).isEqualTo(2L);
    }

    private static AppUser user(Long id) {
        AppUser user = new AppUser("Cliente " + id, "cliente" + id + "@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static Product product(Long id, String price, int stock) {
        Product product = new Product("Produto " + id, null, new BigDecimal(price), stock);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
