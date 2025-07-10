package com.micro.ecommerce.service;

import com.micro.ecommerce.dto.ProductPurchaseRequest;
import com.micro.ecommerce.dto.ProductPurchaseResponse;
import com.micro.ecommerce.dto.ProductRequest;
import com.micro.ecommerce.dto.ProductResponse;
import com.micro.ecommerce.entity.Product;
import com.micro.ecommerce.exception.ProductPurchaseException;
import com.micro.ecommerce.mapper.ProductMapper;
import com.micro.ecommerce.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Integer createProduct(ProductRequest productRequest) {
        return productRepository.save(productMapper.toProduct(productRequest)).getId();
    }

    public List<ProductPurchaseResponse> purchaseProducts(List<ProductPurchaseRequest> purchaseRequestList) {
        var requestMap = toRequestMap(purchaseRequestList);
        var products = fetchAndValidateProductsExist(requestMap);
        validateStockAvailability(products, requestMap);
        updateProductQuantities(products, requestMap);
        productRepository.saveAll(products);
        return mapToPurchaseResponses(products, requestMap);
    }

    public ProductResponse findById(Integer productId) {
        return productRepository.findById(productId)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    private Map<Integer, ProductPurchaseRequest> toRequestMap(List<ProductPurchaseRequest> requests) {
        return requests.stream()
                .collect(Collectors.toMap(ProductPurchaseRequest::productId, r -> r));
    }

    private List<Product> fetchAndValidateProductsExist(Map<Integer, ProductPurchaseRequest> requestMap) {
        var productIds = new ArrayList<>(requestMap.keySet());
        var products = productRepository.findAllByIdInOrderById(productIds);
        if (products.size() != productIds.size()) {
            throw new ProductPurchaseException("One or more products do not exist");
        }
        return products;
    }

    private void validateStockAvailability(List<Product> products, Map<Integer, ProductPurchaseRequest> requestMap) {
        for (var product : products) {
            var requestedQty = requestMap.get(product.getId()).quantity();
            if (product.getAvailableQuantity() < requestedQty) {
                throw new ProductPurchaseException("Insufficient stock for product ID " + product.getId());
            }
        }
    }

    private void updateProductQuantities(List<Product> products, Map<Integer, ProductPurchaseRequest> requestMap) {
        for (var product : products) {
            var requestedQty = requestMap.get(product.getId()).quantity();
            product.setAvailableQuantity(product.getAvailableQuantity() - requestedQty);
        }
    }

    private List<ProductPurchaseResponse> mapToPurchaseResponses(List<Product> products, Map<Integer, ProductPurchaseRequest> requestMap) {
        return products.stream()
                .map(p -> productMapper.toProductPurchaseResponse(p, requestMap.get(p.getId()).quantity()))
                .toList();
    }
}
