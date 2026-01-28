package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.api.order.OrderApi;
import ru.yandex.practicum.api.shoppingStore.ShoppingStoreApi;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.payment.PaymentState;
import ru.yandex.practicum.dto.shoppingStore.ProductDto;
import ru.yandex.practicum.exception.payment.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.repository.PaymentRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderApi orderApi;
    private final ShoppingStoreApi shoppingStoreApi;

    @Override
    public PaymentDto createPayment(OrderDto orderDto) {
        log.info("Создание платежа для заказа: {}", orderDto.getOrderId());
        checkOrderForPayment(orderDto);

        paymentRepository.findByOrderId(orderDto.getOrderId())
                .ifPresent(existing -> {
                    log.warn("Платеж для заказа {} уже существует: {}", orderDto.getOrderId(), existing.getPaymentId());
                    throw new IllegalArgumentException("Платеж для данного заказа уже существует");
                });

        Payment payment = buildPayment(orderDto);
        Payment savedPayment = paymentRepository.save(payment);

        log.info("Платеж создан: {}", savedPayment.getPaymentId());
        return PaymentMapper.INSTANCE.toDto(savedPayment);
    }

    @Override
    public Double calculateProductPrice(OrderDto orderDto) {
        log.info("Расчет стоимости товаров для заказа: {}", orderDto.getOrderId());
        checkProducts(orderDto);

        Double totalProductPrice = orderDto.getProducts().entrySet().stream()
                .mapToDouble(entry -> getProductPrice(entry.getKey()).doubleValue() * entry.getValue())
                .sum();

        log.info("Стоимость товаров для заказа {}: {}", orderDto.getOrderId(), totalProductPrice);
        return Math.round(totalProductPrice * 100.0) / 100.0;
    }

    @Override
    public Double calculateTotalPrice(OrderDto orderDto) {
        log.info("Расчет итоговой стоимости заказа: {}", orderDto.getOrderId());
        checkOrderForCalculation(orderDto);

        Double productPrice = calculateProductPrice(orderDto);
        Double tax = productPrice * 0.1;
        Double deliveryPrice = orderDto.getDeliveryPrice();
        Double totalPrice = productPrice + tax + deliveryPrice;

        log.info("Итоговая стоимость заказа {}: {}", orderDto.getOrderId(), totalPrice);
        return Math.round(totalPrice * 100.0) / 100.0;
    }

    @Override
    public void setPaymentFailed(UUID paymentId) {
        log.info("Установка статуса 'НЕУДАЧНЫЙ' для платежа: {}", paymentId);
        Payment payment = getPaymentOrThrow(paymentId);

        payment.setState(PaymentState.FAILED);
        paymentRepository.save(payment);

        orderApi.setPaymentFailed(payment.getOrderId());
        log.info("Платеж {} помечен как неудачный", paymentId);
    }

    @Override
    public void payOrder(UUID paymentId) {
        log.info("Обработка успешного платежа: {}", paymentId);
        Payment payment = getPaymentOrThrow(paymentId);

        payment.setState(PaymentState.SUCCESS);
        paymentRepository.save(payment);

        orderApi.payOrder(payment.getOrderId());
        log.info("Платеж {} успешно завершен", paymentId);
    }

    private Payment buildPayment(OrderDto orderDto) {
        Double productPrice = calculateProductPrice(orderDto);
        Double tax = productPrice * 0.1;
        Double deliveryPrice = orderDto.getDeliveryPrice();
        Double totalPrice = productPrice + tax + deliveryPrice;

        return Payment.builder()
                .paymentId(UUID.randomUUID())
                .orderId(orderDto.getOrderId())
                .productPrice(productPrice)
                .feePrice(tax)
                .deliveryPrice(deliveryPrice)
                .totalPrice(totalPrice)
                .state(PaymentState.PENDING)
                .build();
    }

    private Double getProductPrice(UUID productId) {
        try {
            ProductDto product = shoppingStoreApi.getProductById(productId);
            return product.getPrice().doubleValue();
        } catch (Exception e) {
            log.error("Ошибка при получении цены продукта {}: {}", productId, e.getMessage());
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Не удалось получить информацию о продукте " + productId
            );
        }
    }

    private Payment getPaymentOrThrow(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Платеж с ID " + paymentId + " не найден"
                ));
    }

    private void checkOrderForPayment(OrderDto orderDto) {
        if (orderDto == null) {
            throw new IllegalArgumentException("Заказ не может быть null");
        }
        if (orderDto.getOrderId() == null) {
            throw new IllegalArgumentException("ID заказа не может быть null");
        }
        if (orderDto.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Не указана стоимость доставки"
            );
        }
        checkProducts(orderDto);
    }

    private void checkOrderForCalculation(OrderDto orderDto) {
        if (orderDto == null) {
            throw new IllegalArgumentException("Заказ не может быть null");
        }
        if (orderDto.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Не указана стоимость доставки"
            );
        }
        checkProducts(orderDto);
    }

    private void checkProducts(OrderDto orderDto) {
        if (orderDto.getProducts() == null || orderDto.getProducts().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Список товаров в заказе пуст"
            );
        }
    }
}
