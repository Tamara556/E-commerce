package com.micro.ecommerce.service;

import com.micro.ecommerce.dto.Product;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private JavaMailSender mailSender;
    private SpringTemplateEngine templateEngine;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        templateEngine = mock(SpringTemplateEngine.class);
        emailService = new EmailService(mailSender, templateEngine);
    }

    @Test
    void testSendPaymentSuccessEmail() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>template</html>");

        emailService.sendPaymentSuccessEmail(
                "test@example.com",
                "John Doe",
                BigDecimal.valueOf(99.99),
                "ORDER123"
        );

        verify(mailSender, times(1)).send(mimeMessage);
        verify(templateEngine, times(1)).process(anyString(), any(Context.class));
    }

    @Test
    void testSendOrderConfirmationEmail() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>template</html>");

        Product product = new Product(1, "Product Name", "Description", BigDecimal.valueOf(10.99), 1);

        emailService.sendOrderConfirmationEmail(
                "customer@example.com",
                "Customer Name",
                BigDecimal.valueOf(150.00),
                "ORDER456",
                List.of(product)
        );

        verify(mailSender, times(1)).send(mimeMessage);
        verify(templateEngine, times(1)).process(anyString(), any(Context.class));
    }

    @Test
    void testSendPaymentSuccessEmail_shouldHandleMessagingException() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new RuntimeException("Failed to send email"))
                .when(mailSender).send(any(MimeMessage.class));

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>template</html>");

        assertThrows(RuntimeException.class, () ->
                emailService.sendPaymentSuccessEmail(
                        "fail@example.com",
                        "John Doe",
                        BigDecimal.valueOf(50),
                        "ORDER789"
                )
        );

        verify(mailSender, times(1)).send(mimeMessage);
    }

}
