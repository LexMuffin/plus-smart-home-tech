package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDto createOrder(String username, CreateNewOrderRequest newOrderRequest);

    List<OrderDto> getUserOrders(String username);

    OrderDto payOrder(UUID orderId);

    OrderDto returnOrder(ProductReturnRequest returnRequest);

    OrderDto setPaymentFailed(UUID orderId);

    OrderDto calculateTotalPrice(UUID orderId);

    OrderDto assembleOrder(UUID orderId);

    OrderDto assembleOrderFailed(UUID orderId);

    OrderDto calculateDeliveryPrice(UUID orderId);

    OrderDto deliveryOrder(UUID orderId);

    OrderDto deliveryOrderFailed(UUID orderId);

    OrderDto completedOrder(UUID orderId);
}
