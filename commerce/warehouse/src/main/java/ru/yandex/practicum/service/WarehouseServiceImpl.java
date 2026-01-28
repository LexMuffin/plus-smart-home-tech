package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.exception.order.NoOrderFoundException;
import ru.yandex.practicum.exception.shoppingCart.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.warehouse.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.warehouse.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.warehouse.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.WarehouseProductMapper;
import ru.yandex.practicum.model.Booking;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.BookingRepository;
import ru.yandex.practicum.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseProductRepository warehouseProductRepository;
    private final BookingRepository bookingRepository;

    private static final String[] ADDRESSES = new String[] {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    @Override
    @Transactional
    public void addNewProduct(NewProductInWarehouseRequest request) {
        log.debug("Добавление нового продукта {}", request);
        if (warehouseProductRepository.existsById(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException("Такой продукт уже существует");
        }
        WarehouseProduct warehouseProduct = WarehouseProductMapper.INSTANCE.toEntity(request);
        if (warehouseProduct.getQuantity() == null) {
            warehouseProduct.setQuantity(0L);
        }
        warehouseProductRepository.save(warehouseProduct);
    }

    @Override
    @Transactional
    public void increaseProductQuantity(AddProductToWarehouseRequest request) {
        log.debug("Добавление количества продукта {}", request);
        WarehouseProduct warehouseProduct = warehouseProductRepository.findById(UUID.fromString(request.getProductId()))
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Такого продукта не существует"));
        warehouseProduct.setQuantity(warehouseProduct.getQuantity() + request.getQuantity());
        warehouseProductRepository.save(warehouseProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public BookedProductsDto checkAvailability(ShoppingCartDto cart) {
        log.debug("Проверка наличия продукта");
        Map<UUID, Integer> items = cart.getProducts();
        if (items == null || items.isEmpty()) {
            return emptyCart();
        }

        Map<UUID, Long> missingProducts = new HashMap<>();
        BookedProductsDto result = calculateCartDetails(items, missingProducts);

        if (!missingProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse(missingProducts.toString());
        }

        return result;
    }

    private BookedProductsDto calculateCartDetails(Map<UUID, Integer> items, Map<UUID, Long> missingProducts) {
        if (items == null || items.isEmpty()) {
            return emptyCart();
        }

        List<WarehouseProduct> products = warehouseProductRepository.findAllByProductIdIn(items.keySet());

        Set<UUID> foundIds = products.stream()
                .map(WarehouseProduct::getProductId)
                .collect(Collectors.toSet());
        log.debug("Проверка наличия продуктов на складе {}", foundIds);

        List<UUID> missingIds = items.keySet().stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new NoSpecifiedProductInWarehouseException("Товары не найдены: " + missingIds);
        }

            double[] calculations = {0, 0, 0};

            products.forEach(product -> {
                        Integer q = items.get(product.getProductId());
                        if (product.getQuantity() < q) {
                            missingProducts.put(product.getProductId(), (long) (q - product.getQuantity()));
                        } else {
                            calculations[0] += product.getWeight() * q;
                            calculations[1] += product.getDimension().volume() * q;
                            calculations[2] = Math.max(
                                    calculations[2],
                                    Boolean.TRUE.equals(product.getFragile()) ? 1.0 : 0.0
                            );
                        }
                    });

        return BookedProductsDto.builder()
                .deliveryWeight(calculations[0])
                .deliveryVolume(calculations[1])
                .fragile(calculations[2] > 0.5)
                .build();
    }

    private BookedProductsDto emptyCart() {
        return BookedProductsDto.builder()
                .deliveryWeight(0.0)
                .deliveryVolume(0.0)
                .fragile(false)
                .build();
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return AddressDto.builder()
                .country(CURRENT_ADDRESS)
                .city(CURRENT_ADDRESS)
                .street(CURRENT_ADDRESS)
                .house(CURRENT_ADDRESS)
                .flat(CURRENT_ADDRESS)
                .build();
    }

    @Override
    @Transactional
    public BookedProductsDto assemblingProductsForOrder(AssemblyProductsForOrderRequest assemblyRequest) {
        log.debug("Сборка продуктов для заказа {}", assemblyRequest);

        Map<UUID, Integer> items = assemblyRequest.getProducts();
        if (items == null || items.isEmpty()) {
            throw new NoProductsInShoppingCartException("Корзина заказа пуста");
        }

        // Получаем и проверяем товары
        List<WarehouseProduct> products = warehouseProductRepository.findAllByProductIdIn(items.keySet());

        Set<UUID> foundIds = products.stream()
                .map(WarehouseProduct::getProductId)
                .collect(Collectors.toSet());

        List<UUID> missingIds = items.keySet().stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new NoSpecifiedProductInWarehouseException("Товары не найдены: " + missingIds);
        }

        // Проверяем количество
        Map<UUID, Long> lowQuantityMap = products.stream()
                .filter(product -> product.getQuantity() < items.get(product.getProductId()))
                .collect(Collectors.toMap(
                        WarehouseProduct::getProductId,
                        product -> (items.get(product.getProductId()) - product.getQuantity())
                ));

        if (!lowQuantityMap.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse(lowQuantityMap.toString());
        }

        // Обновляем количество и рассчитываем результат
        products.forEach(product -> {
            Integer requestedQuantity = items.get(product.getProductId());
            product.setQuantity(product.getQuantity() - requestedQuantity);
        });

        warehouseProductRepository.saveAll(products);

        // Рассчитываем итоговые параметры
        BookedProductsDto result = products.stream()
                .reduce(
                        BookedProductsDto.builder()
                                .deliveryWeight(0.0)
                                .deliveryVolume(0.0)
                                .fragile(false)
                                .build(),
                        (dto, product) -> {
                            Integer quantity = items.get(product.getProductId());
                            return BookedProductsDto.builder()
                                    .deliveryWeight(dto.getDeliveryWeight() + product.getWeight() * quantity)
                                    .deliveryVolume(dto.getDeliveryVolume() + product.getDimension().volume() * quantity)
                                    .fragile(dto.getFragile() || Boolean.TRUE.equals(product.getFragile()))
                                    .build();
                        },
                        (dto1, dto2) -> BookedProductsDto.builder()
                                .deliveryWeight(dto1.getDeliveryWeight() + dto2.getDeliveryWeight())
                                .deliveryVolume(dto1.getDeliveryVolume() + dto2.getDeliveryVolume())
                                .fragile(dto1.getFragile() || dto2.getFragile())
                                .build()
                );

        log.debug("Продукты собраны для заказа: {}", result);
        return result;
    }

    @Override
    @Transactional
    public void returnProductsToWarehouse(Map<UUID, Integer> returnedProducts) {
        log.debug("Возврат продуктов на склад {}", returnedProducts);

        if (returnedProducts == null || returnedProducts.isEmpty()) {
            throw new NoProductsInShoppingCartException("Нет товаров для возврата");
        }

        List<WarehouseProduct> products = warehouseProductRepository.findAllByProductIdIn(returnedProducts.keySet());

        Set<UUID> foundIds = products.stream()
                .map(WarehouseProduct::getProductId)
                .collect(Collectors.toSet());

        List<UUID> missingIds = returnedProducts.keySet().stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new NoSpecifiedProductInWarehouseException("Товары не найдены: " + missingIds);
        }

        // Возвращаем товары на склад
        products.forEach(product -> {
            Integer returnQuantity = returnedProducts.get(product.getProductId());
            if (returnQuantity != null && returnQuantity > 0) {
                product.setQuantity(product.getQuantity() + returnQuantity);
                warehouseProductRepository.save(product);
            }
        });

        log.debug("Товары успешно возвращены на склад");
    }

    @Override
    @Transactional
    public void shippedProductsToWarehouse(ShippedToDeliveryRequest deliveryRequest) {
        log.debug("Отгрузка продуктов со склада {}", deliveryRequest);

        if (deliveryRequest == null || deliveryRequest.getOrderId() == null) {
            throw new IllegalArgumentException("Некорректный запрос на отгрузку");
        }

        // Логика отметки о передаче товаров в доставку
        Booking orderBooking = bookingRepository.findByOrderId(deliveryRequest.getOrderId())
            .orElseThrow(() -> new NoOrderFoundException("Бронирование для заказа не найдено"));

        orderBooking.setDeliveryId(deliveryRequest.getDeliveryId());
        bookingRepository.save(orderBooking);

        log.debug("Заказ {} передан в доставку {}", deliveryRequest.getOrderId(), deliveryRequest.getDeliveryId());
    }
}
