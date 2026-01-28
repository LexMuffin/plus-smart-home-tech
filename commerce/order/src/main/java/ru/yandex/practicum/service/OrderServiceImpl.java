package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.api.delivery.DeliveryApi;
import ru.yandex.practicum.api.payment.PaymentApi;
import ru.yandex.practicum.api.warehouse.WarehouseApi;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.OrderState;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.exception.order.NoAuthorizedUserException;
import ru.yandex.practicum.exception.order.NoOrderFoundException;
import ru.yandex.practicum.exception.payment.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final WarehouseApi warehouseApi;
    private final PaymentApi paymentApi;
    private final DeliveryApi deliveryApi;

    @Override
    public OrderDto createOrder(String username, CreateNewOrderRequest newOrderRequest) {
        if (newOrderRequest == null) {
            throw new IllegalArgumentException("CreateNewOrderRequest не должен быть null");
        }
        if (username == null || username.isBlank()) {
            throw new NoAuthorizedUserException("Username должен быть заполнен");
        }
        log.info("Проверка товара на складе");
        BookedProductsDto bookedProductsDto = warehouseApi.checkAvailability(newOrderRequest.getShoppingCart());
        Order order = collectOrder(username, newOrderRequest, bookedProductsDto);

        log.info("Расчет стоимости");
        Double productCost = paymentApi.calculateProductPrice(OrderMapper.INSTANCE.toDto(order));
        order.setProductPrice(productCost);

        log.info("Добавление доставки");
        DeliveryDto deliveryDto = collectDeliveryOrder(order.getOrderId(), newOrderRequest.getDeliveryAddress());
        order.setDeliveryId(deliveryDto.getDeliveryId());
        order = orderRepository.save(order);

        return OrderMapper.INSTANCE.toDto(order);
    }

    @Override
    public List<OrderDto> getUserOrders(String username) {
        log.info("Получение заказов пользователя: {}", username);
        if (username == null || username.isBlank()) {
            throw new NoAuthorizedUserException("Username должен быть заполнен");
        }
        List<Order> orders = orderRepository.findAllByUsername(username);

        return OrderMapper.INSTANCE.toDtos(orders);
    }

    @Override
    public OrderDto payOrder(UUID orderId) {
        log.info("Обновляем статус заказа {} на оплачен", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ с id: " + orderId));
        order.setState(OrderState.PAID);
        order = orderRepository.save(order);

        return OrderMapper.INSTANCE.toDto(order);
    }

    @Override
    public OrderDto returnOrder(ProductReturnRequest returnRequest) {
        log.info("Обновляем статус заказа {} на возврат и возвращаем продукты на склад", returnRequest.getOrderId());
        Order order = orderRepository.findById(returnRequest.getOrderId())
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ с id: " + returnRequest.getOrderId()));
        order.setState(OrderState.PRODUCT_RETURNED);
        order = orderRepository.save(order);

        warehouseApi.returnProductsToWarehouse(returnRequest.getProducts());
        return OrderMapper.INSTANCE.toDto(order);
    }

    @Override
    public OrderDto setPaymentFailed(UUID orderId) {
        log.info("Обновляем статус заказа {} на ошибку в оплате", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ с id: " + orderId));
        order.setState(OrderState.PAYMENT_FAILED);
        order = orderRepository.save(order);
        return OrderMapper.INSTANCE.toDto(order);
    }

    @Override
    public OrderDto calculateTotalPrice(UUID orderId) {
        log.info("Расчет полной стоимости заказа: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ с id: " + orderId));
        if (order.getDeliveryPrice() == null || order.getProductPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Невозможно рассчитать полную стоимость заказа");
        }
        Double totalCost = paymentApi.calculateTotalPrice(OrderMapper.INSTANCE.toDto(order));
        order.setTotalPrice(totalCost);
        order = orderRepository.save(order);
        return OrderMapper.INSTANCE.toDto(order);
    }

    @Override
    public OrderDto assembleOrder(UUID orderId) {
        log.info("Бронирование заказа {} на складе", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ с id: " + orderId));

        AssemblyProductsForOrderRequest assemblyRequest = AssemblyProductsForOrderRequest.builder()
                .orderId(orderId)
                .products(order.getProducts())
                .build();

        BookedProductsDto bookedDto = warehouseApi.assemblingProductsForOrder(assemblyRequest);
        order.setState(OrderState.ASSEMBLED);
        order = orderRepository.save(order);
        order = createPaymentOrder(order);

        return OrderMapper.INSTANCE.toDto(order);
    }

    @Override
    public OrderDto assembleOrderFailed(UUID orderId) {
        log.info("Ошибка бронирования заказа {} на складе", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ с id: " + orderId));
        order.setState(OrderState.ASSEMBLY_FAILED);
        order = orderRepository.save(order);
        return OrderMapper.INSTANCE.toDto(order);
    }

    @Override
    public OrderDto calculateDeliveryPrice(UUID orderId) {
        log.info("Добавление стоимости доставки для заказа {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ с id: " + orderId));
        Double deliveryPrice = deliveryApi.calculateDelivery(OrderMapper.INSTANCE.toDto(order));
        order.setDeliveryPrice(deliveryPrice);
        order = orderRepository.save(order);
        return OrderMapper.INSTANCE.toDto(order);
    }

    @Override
    public OrderDto deliveryOrder(UUID orderId) {
        log.info("Изменение статуса заказа {} на доставлен", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ с id: " + orderId));
        order.setState(OrderState.DELIVERED);
        order = orderRepository.save(order);
        return OrderMapper.INSTANCE.toDto(order);
    }

    @Override
    public OrderDto deliveryOrderFailed(UUID orderId) {
        log.info("Изменение статуса заказа {} на ошибка доставки", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ с id: " + orderId));
        order.setState(OrderState.DELIVERY_FAILED);
        order = orderRepository.save(order);
        return OrderMapper.INSTANCE.toDto(order);
    }

    @Override
    public OrderDto completedOrder(UUID orderId) {
        log.info("Изменение статуса заказа {} на выполнен", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ с id: " + orderId));
        order.setState(OrderState.COMPLETED);
        order = orderRepository.save(order);
        return OrderMapper.INSTANCE.toDto(order);
    }

    private Order collectOrder(String username, CreateNewOrderRequest newOrderRequest, BookedProductsDto bookedProducts) {
        ShoppingCartDto shoppingCart = newOrderRequest.getShoppingCart();
        return Order.builder()
                .username(username)
                .shoppingCartId(shoppingCart.getShoppingCartId())
                .products(shoppingCart.getProducts())
                .state(OrderState.NEW)
                .deliveryWeight(bookedProducts.getDeliveryWeight())
                .deliveryVolume(bookedProducts.getDeliveryVolume())
                .fragile(bookedProducts.getFragile())
                .build();
    }

    private DeliveryDto collectDeliveryOrder(UUID orderId, AddressDto addressDto) {
        AddressDto fromAddressDelivery = warehouseApi.getWarehouseAddress();
        DeliveryDto dto = DeliveryDto.builder()
                .fromAddress(fromAddressDelivery)
                .toAddress(addressDto)
                .orderId(orderId)
                .deliveryState(DeliveryState.CREATED)
                .build();
        return deliveryApi.createDelivery(dto);
    }

    private Order createPaymentOrder(Order order) {
        OrderDto orderDto = OrderMapper.INSTANCE.toDto(order);
        PaymentDto paymentDto = paymentApi.createPayment(orderDto);
        order.setState(OrderState.ON_PAYMENT);
        order.setPaymentId(paymentDto.getPaymentId());
        return orderRepository.save(order);
    }
}
