package ru.yandex.practicum.api.payment;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.util.UUID;

@FeignClient(name = "payment", path = "/api/v1/payment")
public interface PaymentApi {
    @PostMapping
    PaymentDto createPayment(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/productCost")
    Double calculateProductPrice(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/totalCost")
    Double calculateTotalPrice(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/failed")
    void setPaymentFailed(@RequestBody UUID paymentId);

    @PostMapping("/refund")
    void payOrder(@RequestBody UUID paymentId);
}
