package ru.yandex.practicum.api.order;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "order", path = "/api/v1/order")
public interface OrderApi {
    @PutMapping
    OrderDto createOrder(@RequestParam(name = "username") String username,
                         @Valid @RequestBody CreateNewOrderRequest newOrderRequest);

    @GetMapping
    List<OrderDto> getUserOrders(@RequestParam(name = "username") String username);

    @PostMapping("/payment")
    OrderDto payOrder(@RequestBody UUID orderId);

    @PostMapping("/return")
    OrderDto returnOrder(@Valid @RequestBody ProductReturnRequest returnRequest);

    @PostMapping("/payment/failed")
    OrderDto setPaymentFailed(@RequestBody UUID orderId);

    @PostMapping("/calculate/total")
    OrderDto calculateTotalPrice(@RequestBody UUID orderId);

    @PostMapping("/assembly")
    OrderDto assembleOrder(@RequestBody UUID orderId);

    @PostMapping("/assembly/failed")
    OrderDto assembleOrderFailed(@RequestBody UUID orderId);

    @PostMapping("/calculate/delivery")
    OrderDto calculateDeliveryPrice(@RequestBody UUID orderId);

    @PostMapping("/delivery")
    OrderDto deliveryOrder(@RequestBody UUID orderId);

    @PostMapping("/delivery/failed")
    OrderDto deliveryOrderFailed(@RequestBody UUID orderId);

    @PostMapping("/completed")
    OrderDto completedOrder(@RequestBody UUID orderId);
}
