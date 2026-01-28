package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.warehouse.WarehouseApi;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi {

    private final WarehouseService warehouseService;

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addProduct(@Valid @RequestBody NewProductInWarehouseRequest product) {
        log.debug("Добавление продукта: {}", product);
        warehouseService.addNewProduct(product);
    }

    @Override
    @PostMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    public BookedProductsDto checkAvailability(ShoppingCartDto shoppingCartDto) {
        log.info("Проверка склада для корзины {}", shoppingCartDto.getShoppingCartId());
        return warehouseService.checkAvailability(shoppingCartDto);
    }

    @Override
    @PostMapping("/add")
    @ResponseStatus(HttpStatus.OK)
    public void increaseProductQuantity(@Valid @RequestBody AddProductToWarehouseRequest request) {
        log.debug("Пополнение запасов: {}", request);
        warehouseService.increaseProductQuantity(request);
    }

    @Override
    @GetMapping("/address")
    @ResponseStatus(HttpStatus.OK)
    public AddressDto getWarehouseAddress() {
        log.info("Запрос адреса склада");
        return warehouseService.getWarehouseAddress();
    }

    @Override
    @PostMapping("/assembly")
    @ResponseStatus(HttpStatus.OK)
    public BookedProductsDto assemblingProductsForOrder(@Valid @RequestBody AssemblyProductsForOrderRequest assemblyRequest) {
        log.info("Сборка продуктов для заказа {}", assemblyRequest.getOrderId());
        return warehouseService.assemblingProductsForOrder(assemblyRequest);
    }

    @Override
    @PostMapping("/return")
    @ResponseStatus(HttpStatus.OK)
    public void returnProductsToWarehouse(@Valid @RequestBody Map<UUID, Integer> returnedProducts) {
        log.info("Возврат товаров на склад: {}", returnedProducts);
        warehouseService.returnProductsToWarehouse(returnedProducts);
    }

    @Override
    @PostMapping("/shipped")
    @ResponseStatus(HttpStatus.OK)
    public void shippedProductsToWarehouse(@Valid @RequestBody ShippedToDeliveryRequest deliveryRequest) {
        log.info("Отгрузка товаров для заказа {} в доставку {}",
                deliveryRequest.getOrderId(), deliveryRequest.getDeliveryId());
        warehouseService.shippedProductsToWarehouse(deliveryRequest);
    }
}
