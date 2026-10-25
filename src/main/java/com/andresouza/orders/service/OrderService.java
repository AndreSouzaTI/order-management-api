package com.andresouza.orders.service;

import com.andresouza.orders.domain.AppUser;
import com.andresouza.orders.domain.Order;
import com.andresouza.orders.domain.OrderStatus;
import com.andresouza.orders.domain.Product;
import com.andresouza.orders.exception.ResourceNotFoundException;
import com.andresouza.orders.repository.OrderRepository;
import com.andresouza.orders.repository.ProductRepository;
import com.andresouza.orders.repository.UserRepository;
import com.andresouza.orders.security.CurrentUser;
import com.andresouza.orders.web.dto.OrderDtos.CreateOrderRequest;
import com.andresouza.orders.web.dto.OrderDtos.OrderItemRequest;
import com.andresouza.orders.web.dto.OrderDtos.OrderResponse;
import com.andresouza.orders.web.dto.OrderDtos.OrderSummaryResponse;
import com.andresouza.orders.web.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;

    public OrderService(OrderRepository orders, ProductRepository products, UserRepository users) {
        this.orders = orders;
        this.products = products;
        this.users = users;
    }

    @Transactional
    public OrderResponse create(CurrentUser currentUser, CreateOrderRequest request) {
        AppUser customer = users.findById(currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", currentUser.id()));

        // Itens repetidos do mesmo produto são somados em uma única linha
        Map<Long, Integer> quantities = request.items().stream()
                .collect(Collectors.toMap(OrderItemRequest::productId, OrderItemRequest::quantity, Integer::sum, LinkedHashMap::new));

        Map<Long, Product> found = products.findAllById(quantities.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Order order = new Order(customer);
        quantities.forEach((productId, quantity) -> {
            Product product = found.get(productId);
            if (product == null) {
                throw new ResourceNotFoundException("Produto", productId);
            }
            order.addItem(product, quantity);
        });
        return OrderResponse.from(orders.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(CurrentUser currentUser, Long id) {
        return OrderResponse.from(getVisibleOrder(currentUser, id));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> listMine(CurrentUser currentUser, Pageable pageable) {
        return PageResponse.of(orders.findByCustomerId(currentUser.id(), pageable), OrderSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> listAll(OrderStatus status, Pageable pageable) {
        Page<Order> page = status == null ? orders.findAllBy(pageable) : orders.findByStatus(status, pageable);
        return PageResponse.of(page, OrderSummaryResponse::from);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = orders.findWithItemsById(id).orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
        order.changeStatus(status);
        return OrderResponse.from(order);
    }

    /** O próprio cliente pode cancelar enquanto o pedido não foi enviado. */
    @Transactional
    public OrderResponse cancel(CurrentUser currentUser, Long id) {
        Order order = getVisibleOrder(currentUser, id);
        order.changeStatus(OrderStatus.CANCELLED);
        return OrderResponse.from(order);
    }

    private Order getVisibleOrder(CurrentUser currentUser, Long id) {
        Order order = orders.findWithItemsById(id).orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
        if (!currentUser.admin() && !order.belongsTo(currentUser.id())) {
            throw new AccessDeniedException("Este pedido pertence a outro cliente");
        }
        return order;
    }
}
