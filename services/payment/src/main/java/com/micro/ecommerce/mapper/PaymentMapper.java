package com.micro.ecommerce.mapper;

import com.micro.ecommerce.dto.PaymentRequest;
import com.micro.ecommerce.entity.Payment;
import org.springframework.stereotype.Service;

@Service
public class PaymentMapper {
    public Payment toPayment(PaymentRequest request) {
        return Payment.builder()
                .id(request.id())
                .orderId(request.orderId())
                .paymentMethod(request.paymentMethod())
                .amount(request.amount())
                .build();
    }
}
