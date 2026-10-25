package com.andresouza.orders.web;

import com.andresouza.orders.domain.OrderStatus;
import com.andresouza.orders.security.CurrentUser;
import com.andresouza.orders.service.OrderService;
import com.andresouza.orders.web.dto.OrderDtos.CreateOrderRequest;
import com.andresouza.orders.web.dto.OrderDtos.OrderResponse;
import com.andresouza.orders.web.dto.OrderDtos.OrderSummaryResponse;
import com.andresouza.orders.web.dto.OrderDtos.UpdateStatusRequest;
import com.andresouza.orders.web.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Pedidos")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um pedido para o usuário autenticado e baixa o estoque")
    public OrderResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(CurrentUser.from(jwt), request);
    }

    @GetMapping("/me")
    @Operation(summary = "Lista os pedidos do usuário autenticado")
    public PageResponse<OrderSummaryResponse> listMine(@AuthenticationPrincipal Jwt jwt,
                                                       @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.listMine(CurrentUser.from(jwt), pageable);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todos os pedidos, com filtro opcional por status (ADMIN)")
    public PageResponse<OrderSummaryResponse> listAll(@RequestParam(required = false) OrderStatus status,
                                                      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.listAll(status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um pedido (dono do pedido ou ADMIN)")
    public OrderResponse findById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return orderService.findById(CurrentUser.from(jwt), id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Avança o status do pedido (ADMIN)")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancela o pedido e devolve os itens ao estoque")
    public OrderResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return orderService.cancel(CurrentUser.from(jwt), id);
    }
}
