package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.api.order.OrderApi;
import ru.yandex.practicum.api.warehouse.WarehouseApi;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.exception.delivery.NoDeliveryFoundException;
import ru.yandex.practicum.exception.order.NoOrderFoundException;
import ru.yandex.practicum.exception.payment.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderApi orderApi;
    private final WarehouseApi warehouseApi;

    @Override
    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        log.info("Создание доставки для заказа: {}", deliveryDto.getOrderId());
        if (deliveryDto.getFromAddress() == null || deliveryDto.getToAddress() == null || deliveryDto.getOrderId() == null) {
            throw new NoDeliveryFoundException("");
        }
        Delivery delivery = DeliveryMapper.INSTANCE.toEntity(deliveryDto);
        delivery = deliveryRepository.save(delivery);
        return DeliveryMapper.INSTANCE.toDto(delivery);
    }

    @Override
    public Double calculateDelivery(OrderDto orderDto) {
        log.info("Расчет стоимости доставки для заказа: {}", orderDto.getOrderId());
        if (orderDto == null || orderDto.getDeliveryVolume() == null || orderDto.getDeliveryWeight() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Недостаточно данных для расчета стоимости доставки. " +
                    "Проверьте вес, объем и хрупкость товара.");
        }

        Delivery delivery = deliveryRepository.findByOrderId(orderDto.getOrderId())
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка не найдена"));
        AddressDto warehouseAddress = warehouseApi.getWarehouseAddress();

        return getDeliveryPrice(orderDto, warehouseAddress, delivery);
    }

    @Override
    public void setDeliverySuccessful(UUID orderId) {
        log.info("Установка статуса 'УСПЕШНАЯ ДОСТАВКА' для: {}", orderId);
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка не найдена"));
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        orderApi.deliveryOrder(orderId);
    }

    @Override
    public void setDeliveryFailed(UUID orderId) {
        log.info("Установка статуса 'НЕУДАЧНАЯ ДОСТАВКА' для: {}", orderId);
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка не найдена"));
        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        orderApi.deliveryOrderFailed(orderId);
    }

    @Override
    public void pickOrderForDelivery(UUID orderId) {
        log.info("Бронирование заказа для доставки: {}", orderId);
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка не найдена"));
        ShippedToDeliveryRequest shippedRequest = ShippedToDeliveryRequest.builder()
                .deliveryId(delivery.getDeliveryId())
                .orderId(orderId)
                .build();
        try {
            warehouseApi.shippedProductsToWarehouse(shippedRequest);
            log.info("Обновляем статус на складе и передаем заказ в доставку: {}", shippedRequest);
        } catch (FeignException e) {
            throw new NoOrderFoundException(e.getMessage());
        }
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);
        orderApi.assembleOrder(orderId);
    }

    private Double getDeliveryPrice(OrderDto order, AddressDto warehouse, Delivery delivery) {
        double price = 5.0;

        if (warehouse.getStreet().contains("ADDRESS_1")) price *= 2;
        else if (warehouse.getStreet().contains("ADDRESS_2")) price *= 3;

        if (Boolean.TRUE.equals(order.getFragile())) price *= 1.2;

        price += order.getDeliveryWeight() * 0.3 + order.getDeliveryVolume() * 0.2;

        if (!warehouse.getStreet().equalsIgnoreCase(delivery.getToAddress().getStreet())) {
            price *= 1.2;
        }

        return Math.round(price * 100.0) / 100.0;
    }
}
