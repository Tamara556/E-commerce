package com.micro.ecommerce.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.ecommerce.dto.*;
import com.micro.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductEndpoint.class)
class ProductEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductRequest productRequest;
    private ProductResponse productResponse;
    private ProductPurchaseRequest purchaseRequest;
    private ProductPurchaseResponse purchaseResponse;

    @BeforeEach
    void setUp() {
        productRequest = new ProductRequest(
                null, // ID is usually null when creating a product
                "Product1",
                "Description",
                10.0,
                BigDecimal.valueOf(100),
                1
        );

        productResponse = new ProductResponse(
                1, "Product1", "Description", 10.0,
                BigDecimal.valueOf(100), 1, "CategoryName", "CategoryDescription"
        );

        purchaseRequest = new ProductPurchaseRequest(1, 2.0);
        purchaseResponse = new ProductPurchaseResponse(
                1, "Product1", "Description", BigDecimal.valueOf(100), 2.0
        );
    }

    @Test
    void createProduct_ShouldReturnProductId() throws Exception {
        Mockito.when(productService.createProduct(any(ProductRequest.class))).thenReturn(1);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void purchaseProducts_ShouldReturnPurchaseResponseList() throws Exception {
        List<ProductPurchaseRequest> requests = List.of(purchaseRequest);
        List<ProductPurchaseResponse> responses = List.of(purchaseResponse);

        Mockito.when(productService.purchaseProducts(any())).thenReturn(responses);

        mockMvc.perform(post("/api/v1/products/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].productId").value(1));
    }

    @Test
    void findById_ShouldReturnProductResponse() throws Exception {
        Mockito.when(productService.findById(1)).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Product1"));
    }

    @Test
    void findAll_ShouldReturnProductList() throws Exception {
        Mockito.when(productService.findAll()).thenReturn(List.of(productResponse));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }
}