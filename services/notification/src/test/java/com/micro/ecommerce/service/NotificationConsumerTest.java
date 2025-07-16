package com.micro.ecommerce.service;

import com.micro.ecommerce.dto.PaymentConfirmation;
import com.micro.ecommerce.entity.Notification;
import com.micro.ecommerce.entity.PaymentMethod;
import com.micro.ecommerce.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;

import static com.micro.ecommerce.entity.NotificationType.PAYMENT_CONFIRMATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

class NotificationConsumerTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationConsumer consumer;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void consumePaymentSuccessNotifications_shouldSaveNotificationAndSendEmail() throws MessagingException {
        PaymentConfirmation payment = new PaymentConfirmation(
                "order-123",
                BigDecimal.valueOf(100.0),
                PaymentMethod.PAYPAL,
                "John",
                "Doe",
                "john.doe@example.com"
        );

        consumer.consumePaymentSuccessNotifications(payment);

        verify(repository).save(notificationCaptor.capture());
        Notification savedNotification = notificationCaptor.getValue();

        assertEquals(PAYMENT_CONFIRMATION, savedNotification.getType());
        assertEquals(payment, savedNotification.getPaymentConfirmation());
        assert savedNotification.getNotificationDate() != null;

        verify(emailService).sendPaymentSuccessEmail(
                eq(payment.customerEmail()),
                eq(payment.customerFirstname() + " " + payment.customerLastname()),
                eq(payment.amount()),
                eq(payment.orderReference())
        );
    }
}
