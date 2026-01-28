package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.delivery.DeliveryApi;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.service.DeliveryService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController implements DeliveryApi {

    private final DeliveryService deliveryService;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryDto createDelivery(@Valid @RequestBody DeliveryDto deliveryDto) {
        log.info("Запрос на создание доставки для заказа: {}", deliveryDto.getOrderId());
        return deliveryService.createDelivery(deliveryDto);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public Double calculateDelivery(@Valid @RequestBody OrderDto orderDto) {
        log.info("Запрос на расчет стоимости доставки для заказа: {}", orderDto.getOrderId());
        return deliveryService.calculateDelivery(orderDto);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public void setDeliverySuccessful(@PathVariable UUID orderId) {
        log.info("Запрос на отметку успешной доставки: {}", orderId);
        deliveryService.setDeliverySuccessful(orderId);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public void setDeliveryFailed(@PathVariable UUID orderId) {
        log.info("Запрос на отметку неудачной доставки: {}", orderId);
        deliveryService.setDeliveryFailed(orderId);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public void pickOrderForDelivery(@PathVariable UUID orderId) {
        log.info("Запрос на передачу заказа в доставку: {}", orderId);
        deliveryService.pickOrderForDelivery(orderId);
    }
}