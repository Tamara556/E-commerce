package com.micro.ecommerce.service;

import com.micro.ecommerce.dto.ProductPurchaseRequest;
import com.micro.ecommerce.dto.ProductPurchaseResponse;
import com.micro.ecommerce.dto.ProductRequest;
import com.micro.ecommerce.dto.ProductResponse;
import com.micro.ecommerce.entity.Category;
import com.micro.ecommerce.entity.Product;
import com.micro.ecommerce.exception.ProductPurchaseException;
import com.micro.ecommerce.mapper.ProductMapper;
import com.micro.ecommerce.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateProduct() {
        ProductRequest request = new ProductRequest(null, "Item", "Description", 10, BigDecimal.valueOf(100.0), 1);

        Category category = new Category();
        category.setId(1);
        category.setName("Electronics");
        category.setDescription("Electronics Category");

        Product product = new Product(1, "Item", "Description", 10, BigDecimal.valueOf(100.0), category);

        when(productMapper.toProduct(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);

        Integer id = productService.createProduct(request);

        assertEquals(1, id);
        verify(productRepository).save(product);
    }

    @Test
    void testPurchaseProducts_success() {
        Product product1 = new Product(1, "P1", "desc", 10, BigDecimal.valueOf(100.0), null);
        ProductPurchaseRequest req1 = new ProductPurchaseRequest(1, 2);

        Product product2 = new Product(2, "P2", "desc", 5, BigDecimal.valueOf(150.0), null);
        ProductPurchaseRequest req2 = new ProductPurchaseRequest(2, 3);

        List<ProductPurchaseRequest> requests = List.of(req1, req2);
        List<Product> products = List.of(product1, product2);

        when(productRepository.findAllByIdInOrderById(List.of(1, 2))).thenReturn(products);

        ProductPurchaseResponse resp1 = new ProductPurchaseResponse(1, "P1", "description", BigDecimal.valueOf(100.0), 2);
        ProductPurchaseResponse resp2 = new ProductPurchaseResponse(2, "P2", "description", BigDecimal.valueOf(150.0), 3);

        when(productMapper.toProductPurchaseResponse(product1, 2)).thenReturn(resp1);
        when(productMapper.toProductPurchaseResponse(product2, 3)).thenReturn(resp2);

        List<ProductPurchaseResponse> result = productService.purchaseProducts(requests);

        assertEquals(2, result.size());
        assertEquals(8, product1.getAvailableQuantity());
        assertEquals(2, product2.getAvailableQuantity());
        verify(productRepository).saveAll(products);
    }


    @Test
    void testPurchaseProducts_productMissing_throwsException() {
        ProductPurchaseRequest req1 = new ProductPurchaseRequest(1, 2);
        ProductPurchaseRequest req2 = new ProductPurchaseRequest(2, 3);
        List<ProductPurchaseRequest> requests = List.of(req1, req2);

        List<Product> products = List.of(new Product(1, "P1", "desc", 10, BigDecimal.valueOf(100.0), null));

        when(productRepository.findAllByIdInOrderById(List.of(1, 2))).thenReturn(products);

        assertThrows(ProductPurchaseException.class, () -> productService.purchaseProducts(requests));
    }

    @Test
    void testPurchaseProducts_insufficientStock_throwsException() {
        Product product = new Product(1, "P1", "desc", 1, BigDecimal.valueOf(100.0), null);
        ProductPurchaseRequest request = new ProductPurchaseRequest(1, 5);

        when(productRepository.findAllByIdInOrderById(List.of(1))).thenReturn(List.of(product));

        List<ProductPurchaseRequest> requests = List.of(request);

        assertThrows(ProductPurchaseException.class, () -> productService.purchaseProducts(requests));
    }

    @Test
    void testFindById_found() {
        Product product = new Product(1, "Item", "desc", 10, BigDecimal.valueOf(100.0), null);
        ProductResponse response = new ProductResponse(1, "Item", "desc", 10, BigDecimal.valueOf(100.0), 1, "Electronics", "Electronics Category");

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productMapper.toProductResponse(product)).thenReturn(response);

        ProductResponse result = productService.findById(1);

        assertEquals(1, result.id());
    }

    @Test
    void testFindById_notFound() {
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productService.findById(1));
    }

    @Test
    void testFindAll() {
        Product p1 = new Product(1, "P1", "D1", 5, BigDecimal.valueOf(50.0), null);
        Product p2 = new Product(2, "P2", "D2", 8, BigDecimal.valueOf(70.0), null);

        ProductResponse r1 = new ProductResponse(1, "P1", "D1", 5, BigDecimal.valueOf(50.0), 1, "Category1", "Description1");
        ProductResponse r2 = new ProductResponse(2, "P2", "D2", 8, BigDecimal.valueOf(70.0), 2, "Category2", "Description2");

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));
        when(productMapper.toProductResponse(p1)).thenReturn(r1);
        when(productMapper.toProductResponse(p2)).thenReturn(r2);

        List<ProductResponse> responses = productService.findAll();

        assertEquals(2, responses.size());
    }
}
