package com.micro.ecommerce.service;

import com.micro.ecommerce.dto.Customer;
import com.micro.ecommerce.dto.PaymentNotificationRequest;
import com.micro.ecommerce.dto.PaymentRequest;
import com.micro.ecommerce.entity.Payment;
import com.micro.ecommerce.mapper.PaymentMapper;
import com.micro.ecommerce.notification.NotificationProducer;
import com.micro.ecommerce.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static com.micro.ecommerce.entity.PaymentMethod.PAYPAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreatePayment_shouldSavePaymentAndSendNotification() {
        Customer customer = new Customer("1", "John", "Doe", "john.doe@example.com");
        PaymentRequest request = new PaymentRequest(
                1,
                BigDecimal.valueOf(150.75),
                PAYPAL,
                10,
                "ORD-123",
                customer
        );

        Payment payment = new Payment();
        payment.setId(42);

        when(paymentMapper.toPayment(request)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);

        Integer result = paymentService.createPayment(request);

        assertThat(result).isEqualTo(42);

        verify(paymentMapper).toPayment(request);
        verify(paymentRepository).save(payment);

        PaymentNotificationRequest expectedNotification = new PaymentNotificationRequest(
                request.orderReference(),
                request.amount(),
                request.paymentMethod(),
                customer.firstname(),
                customer.lastname(),
                customer.email()
        );

        verify(notificationProducer).sendNotification(expectedNotification);
    }
}