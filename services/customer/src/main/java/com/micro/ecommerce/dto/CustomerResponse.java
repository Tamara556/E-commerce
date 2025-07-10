package com.micro.ecommerce.dto;

import com.micro.ecommerce.entity.Address;

public record CustomerResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        Address address
) {
}
