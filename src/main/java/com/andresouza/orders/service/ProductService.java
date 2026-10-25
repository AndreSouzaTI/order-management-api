package com.andresouza.orders.service;

import com.andresouza.orders.domain.Product;
import com.andresouza.orders.exception.ResourceNotFoundException;
import com.andresouza.orders.repository.ProductRepository;
import com.andresouza.orders.web.dto.PageResponse;
import com.andresouza.orders.web.dto.ProductDtos.ProductRequest;
import com.andresouza.orders.web.dto.ProductDtos.ProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String name, Pageable pageable) {
        return PageResponse.of(products.findByActiveTrueAndNameContainingIgnoreCase(name, pageable), ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(getActive(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product(request.name(), request.description(), request.price(), request.stockQuantity());
        return ProductResponse.from(products.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getActive(id);
        product.update(request.name(), request.description(), request.price(), request.stockQuantity());
        return ProductResponse.from(product);
    }

    /** Exclusão lógica: o produto continua referenciado nos pedidos antigos. */
    @Transactional
    public void deactivate(Long id) {
        getActive(id).deactivate();
    }

    private Product getActive(Long id) {
        return products.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id));
    }
}
