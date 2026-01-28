package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.order.OrderApi;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto createOrder(@RequestParam String username,
                                @Valid @RequestBody CreateNewOrderRequest newOrderRequest) {
        log.info("Запрос пользователя {} на создание нового заказа", username);
        return orderService.createOrder(username, newOrderRequest);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public List<OrderDto> getUserOrders(@RequestParam String username) {
        log.info("Запрос на получение заказов пользователя: {}", username);
        return orderService.getUserOrders(username);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public OrderDto payOrder(@PathVariable UUID orderId) {
        log.info("Запрос на оплату заказа: {}", orderId);
        return orderService.payOrder(orderId);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public OrderDto returnOrder(@Valid @RequestBody ProductReturnRequest returnRequest) {
        log.info("Запрос на возврат заказа: {}", returnRequest.getOrderId());
        return orderService.returnOrder(returnRequest);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public OrderDto setPaymentFailed(@PathVariable UUID orderId) {
        log.info("Запрос при неудачной оплате заказа: {}", orderId);
        return orderService.setPaymentFailed(orderId);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public OrderDto calculateTotalPrice(@PathVariable UUID orderId) {
        log.info("Запрос на рассчет полной стоимости заказа: {}", orderId);
        return orderService.calculateTotalPrice(orderId);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public OrderDto calculateDeliveryPrice(@PathVariable UUID orderId) {
        log.info("Запрос на рассчет стоимости доставки: {}", orderId);
        return orderService.calculateDeliveryPrice(orderId);
    }

    @Override
    @PostMapping("/{orderId}/assemble")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto assembleOrder(@PathVariable UUID orderId) {
        log.info("Запрос на сбор заказа на складе: {}", orderId);
        return orderService.assembleOrder(orderId);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public OrderDto assembleOrderFailed(@PathVariable UUID orderId) {
        log.info("Запрос при неудачной сборке заказа на складе: {}", orderId);
        return orderService.assembleOrderFailed(orderId);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public OrderDto deliveryOrder(@PathVariable UUID orderId) {
        log.info("Запрос при удачной доставке заказа: {}", orderId);
        return orderService.deliveryOrder(orderId);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public OrderDto deliveryOrderFailed(@PathVariable UUID orderId) {
        log.info("Запрос при неудачной доставке заказа: {}", orderId);
        return orderService.deliveryOrderFailed(orderId);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    public OrderDto completedOrder(@PathVariable UUID orderId) {
        log.info("Запрос на завершение заказа: {}", orderId);
        return orderService.completedOrder(orderId);
    }
}