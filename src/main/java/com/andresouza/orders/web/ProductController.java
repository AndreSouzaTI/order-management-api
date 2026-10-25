package com.andresouza.orders.web;

import com.andresouza.orders.service.ProductService;
import com.andresouza.orders.web.dto.PageResponse;
import com.andresouza.orders.web.dto.ProductDtos.ProductRequest;
import com.andresouza.orders.web.dto.ProductDtos.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Produtos")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Lista produtos ativos, com busca por nome e paginação")
    public PageResponse<ProductResponse> search(@RequestParam(defaultValue = "") String name,
                                                @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return productService.search(name, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um produto pelo id")
    public ProductResponse findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra um produto (ADMIN)")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um produto (ADMIN)")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativa um produto (ADMIN)")
    public void deactivate(@PathVariable Long id) {
        productService.deactivate(id);
    }
}
