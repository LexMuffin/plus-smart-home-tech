package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.exception.warehouse.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.warehouse.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.warehouse.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.WarehouseProductMapper;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService{

    private final WarehouseProductRepository warehouseProductRepository;

    private static final String[] ADDRESSES = new String[] {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    @Override
    @Transactional
    public void addNewProduct(NewProductInWarehouseRequest request) {
        log.debug("Добавление нового продукта {}", request);
        if (warehouseProductRepository.existsById(UUID.fromString(request.getProductId()))) {
            throw new SpecifiedProductAlreadyInWarehouseException("Такой продукт уже существует");
        }
        WarehouseProduct warehouseProduct = WarehouseProductMapper.INSTANCE.toEntity(request);
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
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean anyFragile = false;

        for (Map.Entry<UUID, Integer> entry : items.entrySet()) {
            WarehouseProduct warehouseProduct = warehouseProductRepository.findById(entry.getKey())
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Такого продукта не существует"));

            if (warehouseProduct.getQuantity() < entry.getValue()) {
                missingProducts.put(entry.getKey(), entry.getValue() - warehouseProduct.getQuantity());
            } else {
                totalVolume += warehouseProduct.getDimension().volume() * entry.getValue();
                totalWeight += warehouseProduct.getWeight() * entry.getValue();
                anyFragile = anyFragile || Boolean.TRUE.equals(warehouseProduct.getFragile());
            }
        }

        return BookedProductsDto.builder()
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(anyFragile)
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
}
