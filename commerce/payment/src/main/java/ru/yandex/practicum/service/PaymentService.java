package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {
    PaymentDto createPayment(OrderDto orderDto);

    Double calculateProductPrice(OrderDto orderDto);

    Double calculateTotalPrice(OrderDto orderDto);

    void setPaymentFailed(UUID paymentId);

    void payOrder(UUID paymentId);
}
