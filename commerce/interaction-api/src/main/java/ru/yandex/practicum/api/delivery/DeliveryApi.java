package ru.yandex.practicum.api.delivery;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.order.OrderDto;

import java.util.UUID;

@FeignClient(name = "delivery", path = "/api/v1/delivery")
public interface DeliveryApi {
    @PutMapping
    DeliveryDto createDelivery(@Valid @RequestBody DeliveryDto deliveryDto);

    @PostMapping("/cost")
    Double calculateDelivery(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/successful")
    void setDeliverySuccessful(@RequestBody UUID deliveryId);

    @PostMapping("/failed")
    void setDeliveryFailed(@RequestBody UUID deliveryId);

    @PostMapping("/picked")
    void pickOrderForDelivery(@RequestBody UUID deliveryId);
}
