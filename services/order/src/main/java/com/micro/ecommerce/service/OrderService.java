package com.micro.ecommerce.service;

import com.micro.ecommerce.customer.CustomerClient;
import com.micro.ecommerce.dto.*;
import com.micro.ecommerce.exception.BusinessException;
import com.micro.ecommerce.kafka.OrderProducer;
import com.micro.ecommerce.mapper.OrderMapper;
import com.micro.ecommerce.payment.PaymentClient;
import com.micro.ecommerce.product.ProductClient;
import com.micro.ecommerce.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderLineService orderLineService;
    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final PaymentClient paymentClient;
    private final OrderProducer orderProducer;
    private final ProductClient productClient;
    private final OrderMapper mapper;

    public Integer createOrder(OrderRequest orderRequest) {
        var customer = this.customerClient.findCustomerById(orderRequest.customerId())
                .orElseThrow(() -> new BusinessException("Can't create order:: No customer exists with customer id: " + orderRequest.customerId()));

        var purchasedProducts = this.productClient.purchaseProduct(orderRequest.products());

        var order = this.orderRepository.save(mapper.toOrder(orderRequest));

        for (PurchaseRequest purchaseRequest: orderRequest.products()) {
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }

        var paymentRequest = new PaymentRequest(
                orderRequest.amount(),
                orderRequest.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        );
        paymentClient.requestOrderPayment(paymentRequest);

        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        orderRequest.reference(),
                        orderRequest.amount(),
                        orderRequest.paymentMethod(),
                        customer,
                        purchasedProducts
                )
        );

        return order.getId();
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(mapper::fromOrder)
                .collect(Collectors.toList());
    }

    public OrderResponse findById(Integer orderId) {
        return orderRepository.findById(orderId)
                .map(mapper::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with id: %d", orderId)));
    }
}
