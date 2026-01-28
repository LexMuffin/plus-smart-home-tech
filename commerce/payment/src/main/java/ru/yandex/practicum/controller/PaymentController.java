package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.payment.PaymentApi;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDto createPayment(@Valid @RequestBody OrderDto orderDto) {
        log.info("Запрос на создание оплаты для заказа: {}", orderDto.getOrderId());
        return paymentService.createPayment(orderDto);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public Double calculateProductPrice(@Valid @RequestBody OrderDto orderDto) {
        log.info("Запрос на расчет стоимости товаров для заказа: {}", orderDto.getOrderId());
        return paymentService.calculateProductPrice(orderDto);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public Double calculateTotalPrice(@Valid @RequestBody OrderDto orderDto) {
        log.info("Запрос на расчет полной стоимости заказа: {}", orderDto.getOrderId());
        return paymentService.calculateTotalPrice(orderDto);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public void setPaymentFailed(@PathVariable UUID paymentId) {
        log.info("Запрос на отметку неудачной оплаты: {}", paymentId);
        paymentService.setPaymentFailed(paymentId);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public void payOrder(@PathVariable UUID paymentId) {
        log.info("Запрос на подтверждение успешной оплаты: {}", paymentId);
        paymentService.payOrder(paymentId);
    }
}
