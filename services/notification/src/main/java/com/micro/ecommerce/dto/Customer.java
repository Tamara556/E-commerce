package com.micro.ecommerce.dto;

public record Customer(
        String id,
        String firstname,
        String lastname,
        String email
) {
}
